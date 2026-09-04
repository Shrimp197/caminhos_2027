package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import kotlin.math.max

data class WalkingProgress(
    val currentRouteKm: Double,
    val walkedKm: Double,
    val remainingKm: Double,
    val targetRouteKm: Double,
    val progressRatio: Double,
    val stageId: String?,
    val stageName: String? = null
)

object WalkingProgressCalculator {
    fun calculate(route: Route, walk: Walk, positionKm: Double): WalkingProgress {
        require(walk.routeId == route.id) { "Walk and route must match" }
        require(positionKm >= 0.0) { "positionKm must be >= 0" }

        val current = positionKm.coerceAtMost(route.totalDistanceKm)
        val start = (walk.actualStartKm ?: walk.plannedStartKm ?: 0.0).coerceIn(0.0, route.totalDistanceKm)
        val target = (walk.plannedDestinationKm ?: route.totalDistanceKm).coerceIn(start, route.totalDistanceKm)
        val walked = max(0.0, current - start)
        val remaining = max(0.0, target - current)
        val plannedDistance = target - start
        val ratio = if (plannedDistance == 0.0) 1.0 else (walked / plannedDistance).coerceIn(0.0, 1.0)
        val stage = StageLocator.currentStage(route, current)

        return WalkingProgress(
            currentRouteKm = current,
            walkedKm = walked,
            remainingKm = remaining,
            targetRouteKm = target,
            progressRatio = ratio,
            stageId = stage?.id,
            stageName = stage?.name
        )
    }
}
