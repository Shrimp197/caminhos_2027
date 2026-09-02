package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/** Pure qualification rules: data may exist without being suitable for pilgrim-facing publication. */
object ApoiQualification {
    fun evaluate(apoi: Apoi): PublicationStatus {
        if (apoi.publication.status == PublicationStatus.EXCLUDED) return PublicationStatus.EXCLUDED
        if (apoi.location.routeRelation == RouteRelation.OUTSIDE_ROUTE) return PublicationStatus.EXCLUDED
        if (apoi.location.routeRelation == RouteRelation.LOCATION_UNCERTAIN) return PublicationStatus.REVIEW
        if (apoi.location.precision == LocationPrecision.UNKNOWN) return PublicationStatus.REVIEW
        if (apoi.availability.status == ApoiAvailabilityStatus.HISTORICAL) return PublicationStatus.HISTORICAL
        if (apoi.availability.status == ApoiAvailabilityStatus.EXPIRED || apoi.availability.status == ApoiAvailabilityStatus.CLOSED) return PublicationStatus.CLOSED
        if (apoi.availability.status == ApoiAvailabilityStatus.AWAITING_CONFIRMATION) return PublicationStatus.PUBLISHED_WITH_WARNING
        return when (apoi.publication.status) {
            PublicationStatus.PUBLISHED, PublicationStatus.PUBLISHED_WITH_WARNING -> apoi.publication.status
            else -> PublicationStatus.REVIEW
        }
    }
}
