package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/** Selects the next usable published support point by route distance, not straight-line distance. */
object NextApoiSelector {
    private val allowedPublication = setOf(
        PublicationStatus.PUBLISHED,
        PublicationStatus.PUBLISHED_WITH_WARNING
    )
    private val blockedAvailability = setOf(
        ApoiAvailabilityStatus.HISTORICAL,
        ApoiAvailabilityStatus.EXPIRED,
        ApoiAvailabilityStatus.CLOSED
    )
    private val blockedRouteRelations = setOf(
        RouteRelation.DISTANT_POTENTIAL_SUPPORT,
        RouteRelation.LOCATION_UNCERTAIN,
        RouteRelation.OUTSIDE_ROUTE
    )

    fun next(
        currentRouteKm: Double,
        apoi: List<Apoi>,
        service: com.caminhos2027.v1.core.model.ApoiCategory? = null
    ): Apoi? {
        require(currentRouteKm.isFinite() && currentRouteKm >= 0.0) {
            "currentRouteKm must be finite and >= 0"
        }

        return apoi
            .asSequence()
            .filter { it.publication.status in allowedPublication }
            .filter { it.availability.status !in blockedAvailability }
            .filter { it.location.routeRelation !in blockedRouteRelations }
            .filter { routeKm ->
                val value = routeKm.location.routeKm
                value != null && value.isFinite() && value > currentRouteKm
            }
            .filter { service == null || service in it.services }
            .minByOrNull { it.location.routeKm!! }
    }
}
