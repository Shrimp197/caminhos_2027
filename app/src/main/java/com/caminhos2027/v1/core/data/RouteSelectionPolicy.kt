package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus

/** Deterministic route restoration policy shared by Android persistence and its unit tests. */
object RouteSelectionPolicy {
    fun preferredRouteId(
        walks: List<Walk>,
        selectedRouteId: String?,
        isAvailableRoute: (String) -> Boolean
    ): String? {
        val active = walks
            .asReversed()
            .firstOrNull { it.status == WalkStatus.ACTIVE && isAvailableRoute(it.routeId) }
            ?.routeId
        if (active != null) return active

        selectedRouteId
            ?.takeIf(isAvailableRoute)
            ?.let { return it }

        return walks
            .asReversed()
            .firstOrNull { it.status == WalkStatus.PLANNED && isAvailableRoute(it.routeId) }
            ?.routeId
    }
}
