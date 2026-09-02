package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import kotlin.math.max

/** Derived walking progress. It never changes the official route or stage data. */
data class WalkingProgress(
    val routeKm: Double,
    val distanceFromStartKm: Double?,
    val distanceRemainingKm: Double?,
    val stage: Stage?
)

object WalkingProgressCalculator {
    fun calculate(route: Route, walk: Walk, currentRouteKm: Double): WalkingProgress {
        require(walk.routeId == route.id) { "Walk route does not match route" }
        require(currentRouteKm >= 0.0) { "currentRouteKm must be >= 0" }

        val clampedRouteKm = currentRouteKm.coerceIn(0.0, route.totalDistanceKm)
        val startKm = walk.actualStartKm ?: walk.plannedStartKm
        val destinationKm = walk.plannedDestinationKm ?: route.totalDistanceKm
        val distanceFromStart = startKm?.let { max(0.0, clampedRouteKm - it) }
        val distanceRemaining = max(0.0, destinationKm - clampedRouteKm)

        return WalkingProgress(
            routeKm = clampedRouteKm,
            distanceFromStartKm = distanceFromStart,
            distanceRemainingKm = distanceRemaining,
            stage = findStage(route.stages, clampedRouteKm)
        )
    }

    /** Stage boundaries are deterministic: start inclusive, end exclusive, except the final stage. */
    fun findStage(stages: List<Stage>, routeKm: Double): Stage? {
        if (stages.isEmpty()) return null
        val lastIndex = stages.lastIndex
        return stages.withIndex().firstOrNull { (index, stage) ->
            val start = stage.startRouteKm
            val end = stage.endRouteKm
            routeKm >= start && (routeKm < end || (index == lastIndex && routeKm <= end))
        }?.value
    }
}
