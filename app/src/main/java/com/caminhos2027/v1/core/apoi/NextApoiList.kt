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
    ): List<ApoiAhead> {
        require(currentRouteKm.isFinite() && currentRouteKm >= 0.0) {
            "currentRouteKm must be finite and >= 0"
        }
        return records
            .asSequence()
            .filter { it.location.routeId == routeId }
            .filter {
                val routeKm = it.location.routeKm
                routeKm != null && routeKm.isFinite() && routeKm >= currentRouteKm
            }
            .filter { ApoiEligibility.isEligibleForWalking(it) }
            .map { it to (it.location.routeKm!! - currentRouteKm) }
            .sortedBy { it.second }
            .take(limit.coerceAtLeast(0))
            .map { ApoiAhead(it.first, it.second) }
            .toList()
    }
}
