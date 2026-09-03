package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi

/** Finds the next publishable APOI along the route. */
object NextApoiFinder {
    fun findNext(records: List<Apoi>, routeId: String, currentRouteKm: Double): Apoi? =
        records
            .asSequence()
            .filter { it.location.routeId == routeId }
            .filter { it.location.routeKm != null && it.location.routeKm >= currentRouteKm }
            .filter { ApoiEligibility.isEligibleForWalking(it) }
            .minByOrNull { it.location.routeKm ?: Double.POSITIVE_INFINITY }
}
