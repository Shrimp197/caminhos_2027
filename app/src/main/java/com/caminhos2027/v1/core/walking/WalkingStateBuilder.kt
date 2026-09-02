package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgressCalculator
import com.caminhos2027.v1.core.apoi.NextApoiFinder

/** Builds the single walking read model from domain components. */
object WalkingStateBuilder {
    fun build(
        route: Route,
        walk: Walk,
        gpsState: GpsState,
        routePosition: com.caminhos2027.v1.core.model.RoutePosition?,
        publishedApoi: List<Apoi>,
        offline: Boolean = false
    ): WalkingState {
        val progress = routePosition?.let {
            WalkingProgressCalculator.calculate(route, walk, it.routeKm, it.stageId)
        }
        val nextApoi = routePosition?.let {
            NextApoiFinder.findNext(publishedApoi, route.id, it.routeKm)
        }
        return WalkingState(
            walk = walk,
            routePosition = routePosition,
            gpsState = gpsState,
            progress = progress,
            nextApoi = nextApoi,
            isOffline = offline
        )
    }
}
