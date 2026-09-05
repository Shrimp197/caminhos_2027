package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.route.GpsObservation
import com.caminhos2027.v1.core.route.GpsStateEvaluator
import com.caminhos2027.v1.core.route.GpsTrackingPolicy
import com.caminhos2027.v1.core.route.GpsTrackingState
import com.caminhos2027.v1.core.route.RouteLocationEngine
import java.time.Instant

/** Bridges raw device positions to route-aware tracking without knowing anything about Compose. */
class WalkingLocationPipeline(
    private val route: Route,
    private val policy: GpsTrackingPolicy = GpsTrackingPolicy(),
    private val clock: () -> Instant = Instant::now,
    initialState: GpsTrackingState = GpsTrackingState(com.caminhos2027.v1.core.route.GpsState.NO_SIGNAL)
) {
    var trackingState: GpsTrackingState = initialState
        private set

    /**
     * Seeds the tracker with the route position selected at walking start.
     * A provisional seed is intentionally not treated as reliable until a subsequent
     * GPS observation passes the normal evaluator rules.
     */
    fun seedRoutePosition(
        position: RoutePosition,
        capturedAt: Instant,
        reliable: Boolean = true
    ): GpsTrackingState {
        require(position.routeId == route.id) { "Seed position route must match route" }
        val observation = GpsObservation(
            routePosition = position,
            accuracyMeters = null,
            capturedAt = capturedAt
        )
        trackingState = GpsTrackingState(
            state = com.caminhos2027.v1.core.route.GpsState.ACQUIRING,
            lastReliableObservation = observation.takeIf { reliable },
            lastObservation = observation
        )
        return trackingState
    }

    fun accept(position: RawGpsPosition): GpsTrackingState {
        val routePosition = RouteLocationEngine.locate(route, position)
        val observation = GpsObservation(
            routePosition = routePosition,
            accuracyMeters = position.accuracyMeters,
            capturedAt = position.capturedAt
        )
        trackingState = GpsStateEvaluator.update(
            previous = trackingState,
            observation = observation,
            now = clock(),
            policy = policy
        )
        return trackingState
    }

    fun markNoSignal(now: Instant): GpsTrackingState {
        trackingState = GpsStateEvaluator.update(
            previous = trackingState,
            observation = null,
            now = now,
            policy = policy
        )
        return trackingState
    }
}
