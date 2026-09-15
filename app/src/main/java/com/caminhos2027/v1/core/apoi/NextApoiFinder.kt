package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi

/** Finds the next publishable APOI along the route. */
object NextApoiFinder {
    fun findNext(records: List<Apoi>, routeId: String, currentRouteKm: Double): Apoi? {
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
            .minByOrNull { it.location.routeKm ?: Double.POSITIVE_INFINITY }
    }
}
