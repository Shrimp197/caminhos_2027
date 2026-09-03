package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi

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
        .filter { ApoiEligibility.isEligibleForWalking(it) }
        .map { it to (it.location.routeKm!! - currentRouteKm).coerceAtLeast(0.0) }
        .sortedBy { it.second }
        .take(limit.coerceAtLeast(0))
        .map { ApoiAhead(it.first, it.second) }
        .toList()
}
