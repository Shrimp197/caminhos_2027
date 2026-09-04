package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.GpsTrackingPolicy
import com.caminhos2027.v1.core.route.WalkingMovementCueEvaluator
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
    private var lastReliableRouteKm: Double? = null

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
        lastReliableRouteKm = position.routeKm.takeIf { reliable }
        state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = GpsState.ACQUIRING,
            routePosition = position,
            publishedApoi = publishedApoi,
            movementCue = null,
            offline = state.isOffline
        )
        return state
    }

    /** Restores persisted device state, rejecting malformed route positions instead of promoting them to active state. */
    fun restoreCheckpoint(checkpoint: WalkingCheckpoint, now: Instant = Instant.now()): WalkingState {
        val validPosition = checkpoint.routePosition?.takeIf(::isValidRoutePosition)
        lastReliableRouteKm = null
        if (validPosition != null && checkpoint.lastObservedAt != null) {
            locationPipeline.seedRoutePosition(
                validPosition,
                checkpoint.lastObservedAt,
                reliable = true
            )
            lastReliableRouteKm = validPosition.routeKm
        }
        state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = if (validPosition != null) checkpoint.gpsState else GpsState.NO_SIGNAL,
            routePosition = validPosition,
            publishedApoi = publishedApoi,
            movementCue = null,
            offline = checkpoint.isOffline
        )
        return state
    }

    fun accept(position: com.caminhos2027.v1.core.model.RawGpsPosition): WalkingState {
        val previousReliableRouteKm = lastReliableRouteKm
        val tracking = locationPipeline.accept(position)
        val currentReliableRouteKm = tracking.lastReliableObservation?.routePosition?.routeKm
        // Movement requires two reliable observations. Before that, null means "no reference
        // available" rather than manufacturing an UNKNOWN movement label for the UI.
        val movementCue = if (previousReliableRouteKm != null && currentReliableRouteKm != null) {
            WalkingMovementCueEvaluator.evaluate(previousReliableRouteKm, currentReliableRouteKm)
        } else {
            state.movementCue
        }
        if (currentReliableRouteKm != null) {
            lastReliableRouteKm = currentReliableRouteKm
        }
        return rebuild(
            gpsState = tracking.state,
            routePosition = tracking.lastReliableObservation?.routePosition ?: state.routePosition,
            movementCue = movementCue,
            offline = state.isOffline
        )
    }

    fun markNoSignal(now: Instant): WalkingState {
        val tracking = locationPipeline.markNoSignal(now)
        return rebuild(
            gpsState = tracking.state,
            routePosition = tracking.lastReliableObservation?.routePosition ?: state.routePosition,
            movementCue = state.movementCue,
            offline = state.isOffline
        )
    }

    fun setOffline(offline: Boolean): WalkingState = rebuild(
        gpsState = state.gpsState,
        routePosition = state.routePosition,
        movementCue = state.movementCue,
        offline = offline
    )

    private fun rebuild(
        gpsState: GpsState,
        routePosition: RoutePosition?,
        movementCue: com.caminhos2027.v1.core.route.WalkingMovementCue?,
        offline: Boolean
    ): WalkingState {
        state = WalkingStateBuilder.build(
            route = route,
            walk = walk,
            gpsState = gpsState,
            routePosition = routePosition,
            publishedApoi = publishedApoi,
            movementCue = movementCue,
            offline = offline
        )
        return state
    }

    private fun isValidRoutePosition(position: RoutePosition): Boolean =
        position.routeId == route.id &&
            position.routeKm.isFinite() &&
            position.routeKm in 0.0..route.totalDistanceKm &&
            position.distanceToRouteMeters.isFinite() &&
            position.distanceToRouteMeters >= 0.0
}
