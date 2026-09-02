package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/** Domain-level publication guard. It never invents missing information. */
object ApoiValidator {
    fun validateDataset(environment: String, records: List<Apoi>): List<String> {
        val errors = mutableListOf<String>()
        if (environment !in setOf("production", "sr", "hf")) {
            errors += "invalid environment: $environment"
        }
        val duplicateIds = records.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
        duplicateIds.forEach { errors += "duplicate APOI id: $it" }
        records.forEachIndexed { index, apoi ->
            validate(apoi, environment).forEach { errors += "apoi[$index] $it" }
        }
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
        if (location.precision == LocationPrecision.UNKNOWN && (location.latitude != null || location.longitude != null)) {
            errors += "unknown location precision cannot carry coordinates"
        }
        if (location.routeKm != null && location.routeKm < 0.0) errors += "routeKm must be >= 0"
        if (location.distanceToRouteM != null && location.distanceToRouteM < 0.0) errors += "distanceToRouteM must be >= 0"
        if (location.accessDistanceM != null && location.accessDistanceM < 0.0) errors += "accessDistanceM must be >= 0"

        if (environment == "production" && apoi.publication.status in setOf(PublicationStatus.CANDIDATE, PublicationStatus.REVIEW, PublicationStatus.HISTORICAL, PublicationStatus.CLOSED, PublicationStatus.EXCLUDED)) {
            errors += "non-publishable status cannot enter production dataset"
        }

        if (apoi.publication.status == PublicationStatus.PUBLISHED || apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
            if (apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING && apoi.publication.reason.isNullOrBlank()) {
                errors += "published_with_warning requires a reason"
            }
            if (apoi.publication.status == PublicationStatus.PUBLISHED) {
                if (apoi.publication.reason?.contains("awaiting_confirmation", ignoreCase = true) == true) errors += "awaiting confirmation cannot be published"
                if (apoi.publication.reason?.contains("conflict", ignoreCase = true) == true) errors += "critical conflict cannot be published"
            }
            if (apoi.location.precision == LocationPrecision.UNKNOWN) errors += "published APOI requires a sufficiently identified location"
            if (apoi.location.routeRelation == RouteRelation.LOCATION_UNCERTAIN || apoi.location.routeRelation == RouteRelation.OUTSIDE_ROUTE) {
                errors += "location relation is not suitable for normal publication"
            }
        }

        return errors
    }

    fun validatePublicationCandidate(apoi: Apoi): List<String> {
        val errors = validate(apoi, "production").toMutableList()
        if (apoi.mainCategory !in ApoiCategory.entries) errors += "invalid category"
        return errors
    }
}
