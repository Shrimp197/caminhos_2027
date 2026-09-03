package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/** Read model for the support points immediately ahead of the pilgrim. */
data class ApoiAhead(
    val apoi: Apoi,
    val distanceKm: Double
)

object NextApoiList {
    fun findAhead(
        records: List<Apoi>,
        routeId: String,
        currentRouteKm: Double,
        limit: Int = 8
    ): List<ApoiAhead> = records
        .asSequence()
        .filter { it.location.routeId == routeId }
        .filter { it.location.routeKm != null && it.location.routeKm!! >= currentRouteKm }
        .filter { it.publication.status == PublicationStatus.PUBLISHED || it.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING }
        .filter { it.availability.status !in setOf(ApoiAvailabilityStatus.HISTORICAL, ApoiAvailabilityStatus.EXPIRED, ApoiAvailabilityStatus.CLOSED) }
        .filter { it.location.routeRelation != RouteRelation.DISTANT_POTENTIAL_SUPPORT && it.location.routeRelation != RouteRelation.OUTSIDE_ROUTE }
        .map { it to (it.location.routeKm!! - currentRouteKm).coerceAtLeast(0.0) }
        .sortedBy { it.second }
        .take(limit.coerceAtLeast(0))
        .map { ApoiAhead(it.first, it.second) }
        .toList()
}
