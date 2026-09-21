package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.NextApoiFinder
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingMovementCue
import com.caminhos2027.v1.core.route.WalkingProgressCalculator

/** Builds the single walking read model from domain components. */
object WalkingStateBuilder {
    fun build(
        route: Route,
        walk: Walk,
        gpsState: GpsState,
        routePosition: RoutePosition?,
        publishedApoi: List<Apoi>,
        movementCue: WalkingMovementCue? = null,
        offline: Boolean = false,
        paused: Boolean = false
    ): WalkingState {
        val progress = routePosition?.let {
            WalkingProgressCalculator.calculate(route, walk, it.routeKm)
        }
        val nextApoi = routePosition?.let {
            NextApoiFinder.findNext(publishedApoi, route.id, it.routeKm)
        }
        val nextApoiDistanceKm = nextApoi?.location?.routeKm?.let {
            (it - routePosition.routeKm).coerceAtLeast(0.0)
        }
        val pauseRecommendation = routePosition?.let { pauseRecommendation(walk, it.routeKm) }

        return WalkingState(
            walk = walk,
            routePosition = routePosition,
            gpsState = gpsState,
            progress = progress,
            nextApoi = nextApoi,
            nextApoiDistanceKm = nextApoiDistanceKm,
            pauseRecommendation = pauseRecommendation,
            movementCue = movementCue,
            isOffline = offline,
            isPaused = paused
        )
    }
    private fun pauseRecommendation(walk: Walk, currentRouteKm: Double): String? {
        val preparation = walk.preparation
        if (!preparation.intelligentBreaksEnabled) return null
        val threshold = preparation.customBreakDistanceKm?.takeIf { it.isFinite() && it > 0.0 } ?: 5.0
        val startKm = walk.actualStartKm ?: walk.plannedStartKm ?: currentRouteKm
        val distanceSinceStart = (currentRouteKm - startKm).coerceAtLeast(0.0)
        if (distanceSinceStart + 1e-9 < threshold) return null
        return "Pausa recomendada · já percorreu " + String.format(java.util.Locale("pt", "PT"), "%.1f", distanceSinceStart) + " km"
    }
}
