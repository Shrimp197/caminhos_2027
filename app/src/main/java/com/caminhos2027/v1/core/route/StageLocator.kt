package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage

/** Resolves an official stage from route position without changing stage data. */
object StageLocator {
    fun currentStage(route: Route, routeKm: Double): Stage? {
        require(routeKm >= 0.0) { "routeKm must be >= 0" }
        val clamped = routeKm.coerceAtMost(route.totalDistanceKm)

        // At a shared boundary, the later official stage owns the boundary.
        return route.stages
            .sortedBy { it.startRouteKm }
            .firstOrNull { stage ->
                val isLast = stage == route.stages.maxByOrNull { it.endRouteKm }
                clamped >= stage.startRouteKm &&
                    (clamped < stage.endRouteKm || (isLast == true && clamped <= stage.endRouteKm))
            }
    }
}
