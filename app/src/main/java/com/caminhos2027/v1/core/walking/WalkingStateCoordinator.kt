package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.GpsTrackingPolicy
import com.caminhos2027.v1.gps.WalkingLocationPipeline
import java.time.Instant

/** Single coordinator for the active walking read model. */
class WalkingStateCoordinator(
    private val route: com.caminhos2027.v1.core.model.Route,
    initialWalk: Walk,
    publishedApoi: List<Apoi>,
    private val policy: GpsTrackingPolicy = GpsTrackingPolicy()
) {
    private val publishedApoi = publishedApoi.toList()
    private val locationPipeline = WalkingLocationPipeline(route, policy)
    private var walk: Walk = initialWalk

    var state: WalkingState = WalkingStateBuilder.build(
        route = route,
        walk = walk,
        gpsState = GpsState.NO_SIGNAL,
        routePosition = null,
        publishedApoi = this.publishedApoi
    )
        private set

    /** Timestamp of the last observation accepted as reliable by the GPS pipeline. */
    fun lastReliableObservedAt(): Instant? =
        locationPipeline.trackingState.lastReliableObservation?.capturedAt

    /** Starts the planned walk and establishes the supplied real route position as the tracking baseline. */
    fun start(startPosition: RoutePosition, now: Instant = Instant.now()): WalkingState {
        walk = WalkingSessionController.start(walk, startPosition, now)
        seedStartPosition(startPosition, now)
        return state
    }

    /** Compatibility entry point for tests/consumers that already own the lifecycle transition. */
    fun seedStartPosition(position: RoutePosition, now: Instant = Instant.now()): WalkingState {
        require(walk.status == WalkStatus.ACTIVE) {
            "Walking must be active before seeding a start position"
        }
        require(position.routeId == route.id) { "Start position route must match route" }
        // A position already outside the possible-deviation threshold is only a provisional
        // visual anchor. Do not use it as the GPS continuity baseline or persisted observation.
        val reliable = position.distanceToRouteMeters < policy.possibleDeviationMeters
        locationPipeline.seedRoutePosition(position, now, reliable = reliable)
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

    /** Restores persisted device state and uses its last real observation time as the GPS continuity baseline. */
    fun restoreCheckpoint(checkpoint: WalkingCheckpoint, now: Instant = Instant.now()): WalkingState {
        checkpoint.routePosition?.let {
            locationPipeline.seedRoutePosition(
                it,
                checkpoint.lastObservedAt ?: now,
                reliable = checkpoint.lastObservedAt != null
            )
        }
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

    fun accept(position: com.caminhos2027.v1.core.model.RawGpsPosition): WalkingState {
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
