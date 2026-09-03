package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.NextApoiFinder
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgressCalculator

/** Builds the single walking read model from domain components. */
object WalkingStateBuilder {
    fun build(
        route: Route,
        walk: Walk,
        gpsState: GpsState,
        routePosition: RoutePosition?,
        publishedApoi: List<Apoi>,
        offline: Boolean = false
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

        return WalkingState(
            walk = walk,
            routePosition = routePosition,
            gpsState = gpsState,
            progress = progress,
            nextApoi = nextApoi,
            nextApoiDistanceKm = nextApoiDistanceKm,
            isOffline = offline
        )
    }
}
