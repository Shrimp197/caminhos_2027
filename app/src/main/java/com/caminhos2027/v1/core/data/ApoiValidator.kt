package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.CostModel
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.ReservationPolicy

/** Domain-level publication guard. It never invents missing information. */
object ApoiValidator {
    fun validateDataset(environment: String, records: List<Apoi>): List<String> {
        val errors = mutableListOf<String>()
        if (environment !in setOf("production", "sr", "hf")) errors += "invalid environment: $environment"
        records.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys.forEach { errors += "duplicate APOI id: $it" }
        records.forEachIndexed { index, apoi -> validate(apoi, environment).forEach { errors += "apoi[$index] $it" } }
        return errors
    }

    fun validate(apoi: Apoi, environment: String): List<String> {
        val errors = mutableListOf<String>()
        if (apoi.id.isBlank()) errors += "id must not be blank"
        if (apoi.name.isBlank()) errors += "name must not be blank"
        if (apoi.services.isEmpty()) errors += "services must not be empty"
        if (apoi.mainCategory !in apoi.services) errors += "main category must be included in services"

        val location = apoi.location
        if (location.precision == LocationPrecision.EXACT || location.precision == LocationPrecision.APPROXIMATE) {
            if (location.latitude == null || location.longitude == null) errors += "coordinates required for exact/approximate location"
        }
        if (location.precision == LocationPrecision.UNKNOWN && (location.latitude != null || location.longitude != null)) errors += "unknown location precision cannot carry coordinates"
        if (location.routeKm != null && location.routeKm < 0.0) errors += "routeKm must be >= 0"
        if (location.distanceToRouteM != null && location.distanceToRouteM < 0.0) errors += "distanceToRouteM must be >= 0"
        if (location.accessDistanceM != null && location.accessDistanceM < 0.0) errors += "accessDistanceM must be >= 0"

        val support = apoi.support
        if (support.capacity != null && support.capacity < 0) errors += "capacity must be >= 0"
        if (support.waterPotable == true && support.waterPotableConfirmed != true) errors += "potable water must be explicitly confirmed"
        if (support.waterPotableConfirmed == true && support.waterAvailable != true) errors += "potable confirmation requires waterAvailable=true"

        val cost = apoi.cost
        if (cost.model == CostModel.PAID && cost.amount != null && cost.amount < 0.0) errors += "paid cost amount must be >= 0"
        if (cost.model == CostModel.FREE && cost.amount != null) errors += "free cost cannot specify an amount"
        if (apoi.reservation.policy == ReservationPolicy.REQUIRED && apoi.reservation.contact.isNullOrBlank() && apoi.reservation.url.isNullOrBlank()) errors += "required reservation should provide contact or url"

        if (environment == "production" && apoi.publication.status in setOf(PublicationStatus.CANDIDATE, PublicationStatus.REVIEW, PublicationStatus.HISTORICAL, PublicationStatus.CLOSED, PublicationStatus.EXCLUDED)) {
            errors += "non-publishable status cannot enter production dataset"
        }

        if (apoi.publication.status == PublicationStatus.PUBLISHED || apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
            if (apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING && apoi.publication.reason.isNullOrBlank()) errors += "published_with_warning requires a reason"
            if (apoi.publication.status == PublicationStatus.PUBLISHED && apoi.publication.reason?.contains("awaiting_confirmation", ignoreCase = true) == true) errors += "awaiting confirmation cannot be published"
            if (apoi.publication.status == PublicationStatus.PUBLISHED && apoi.publication.reason?.contains("conflict", ignoreCase = true) == true) errors += "critical conflict cannot be published"
            if (location.precision == LocationPrecision.UNKNOWN) errors += "published APOI requires a sufficiently identified location"
            if (location.routeRelation == RouteRelation.LOCATION_UNCERTAIN || location.routeRelation == RouteRelation.OUTSIDE_ROUTE) errors += "location relation is not suitable for normal publication"
            if (apoi.support.pilgrimSupportConfirmed != true) errors += "published APOI requires confirmed pilgrim support"
        }
        return errors
    }

    fun validatePublicationCandidate(apoi: Apoi): List<String> = validate(apoi, "production")
}
