package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.GpsTrackingPolicy
import com.caminhos2027.v1.gps.WalkingLocationPipeline
import java.time.Instant

/** Single coordinator for the active walking read model. */
class WalkingStateCoordinator(
    private val route: com.caminhos2027.v1.core.model.Route,
    private val walk: Walk,
    publishedApoi: List<Apoi>,
    policy: GpsTrackingPolicy = GpsTrackingPolicy()
) {
    private val publishedApoi = publishedApoi.toList()
    private val locationPipeline = WalkingLocationPipeline(route, policy)

    var state: WalkingState = WalkingStateBuilder.build(
        route = route,
        walk = walk,
        gpsState = GpsState.NO_SIGNAL,
        routePosition = null,
        publishedApoi = this.publishedApoi
    )
        private set

    /** Seeds the state with the route position selected when the walk starts. No fake GPS coordinates are used. */
    fun seedStartPosition(position: RoutePosition): WalkingState {
        require(position.routeId == route.id) { "Start position route must match route" }
        state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = GpsState.ACQUIRING,
            routePosition = position,
            publishedApoi = publishedApoi,
            offline = state.isOffline
        )
        return state
    }

    /** Restores only persisted device-derived state; derived APOI/progress values are rebuilt. */
    fun restoreCheckpoint(checkpoint: WalkingCheckpoint): WalkingState {
        state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = checkpoint.gpsState,
            routePosition = checkpoint.routePosition,
            publishedApoi = publishedApoi,
            offline = checkpoint.isOffline
        )
        return state
    }

    fun accept(position: RawGpsPosition): WalkingState {
        val tracking = locationPipeline.accept(position)
        return rebuild(
            gpsState = tracking.state,
            routePosition = tracking.lastReliableObservation?.routePosition ?: state.routePosition,
            offline = state.isOffline
        )
    }

    fun markNoSignal(now: Instant): WalkingState {
        val tracking = locationPipeline.markNoSignal(now)
        return rebuild(
            gpsState = tracking.state,
            routePosition = tracking.lastReliableObservation?.routePosition ?: state.routePosition,
            offline = state.isOffline
        )
    }

    fun setOffline(offline: Boolean): WalkingState = rebuild(
        gpsState = state.gpsState,
        routePosition = state.routePosition,
        offline = offline
    )

    private fun rebuild(
        gpsState: GpsState,
        routePosition: RoutePosition?,
        offline: Boolean
    ): WalkingState {
        state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = gpsState,
            routePosition = routePosition,
            publishedApoi = publishedApoi,
            offline = offline
        )
        return state
    }
}
