package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.gps.WalkingLocationPipeline

/** Coordinates GPS observations, the walking read model, and checkpoint persistence. */
class WalkingStateCoordinator(
    private val route: Route,
    private val walk: Walk,
    private val publishedApoi: List<Apoi>,
    private val sessionService: WalkingSessionService,
    private val locationPipeline: WalkingLocationPipeline = WalkingLocationPipeline(route)
) {
    private var currentState: WalkingState? = null

    fun acceptGps(position: RawGpsPosition, offline: Boolean = false): WalkingState {
        val tracking = locationPipeline.accept(position)
        val reliable = tracking.lastReliableObservation
        val state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = tracking.state,
            routePosition = reliable?.routePosition,
            publishedApoi = publishedApoi,
            offline = offline
        )
        currentState = state
        if (walk.status == com.caminhos2027.v1.core.model.WalkStatus.ACTIVE) {
            sessionService.updatePosition(walk.id, state)
        }
        return state
    }

    fun markNoSignal(now: java.time.Instant, offline: Boolean = false): WalkingState {
        val tracking = locationPipeline.markNoSignal(now)
        val state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = tracking.state,
            routePosition = tracking.lastReliableObservation?.routePosition,
            publishedApoi = publishedApoi,
            offline = offline
        )
        currentState = state
        if (walk.status == com.caminhos2027.v1.core.model.WalkStatus.ACTIVE) {
            sessionService.updatePosition(walk.id, state)
        }
        return state
    }

    fun current(): WalkingState? = currentState
}
