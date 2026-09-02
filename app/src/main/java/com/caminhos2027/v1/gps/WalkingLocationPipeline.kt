package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
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
    initialState: GpsTrackingState = GpsTrackingState(com.caminhos2027.v1.core.route.GpsState.NO_SIGNAL)
) {
    var trackingState: GpsTrackingState = initialState
        private set

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
            now = position.capturedAt,
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
