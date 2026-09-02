package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsTrackingPolicy
import java.time.Instant

/** Coordinates a prepared walk, its lifecycle and its current read state. */
class WalkingSessionRuntime(
    private val route: Route,
    private val sessionService: WalkingSessionService,
    private val publishedApoi: List<com.caminhos2027.v1.core.model.Apoi>,
    private val policy: GpsTrackingPolicy = GpsTrackingPolicy()
) {
    private var coordinator: WalkingStateCoordinator? = null

    fun prepare(walk: Walk): Walk = sessionService.prepare(walk)

    fun start(walkId: String, position: RoutePosition, now: Instant = Instant.now()): WalkingState {
        val started = sessionService.start(walkId, position, now)
        val nextCoordinator = WalkingStateCoordinator(route, started, publishedApoi, policy)
        coordinator = nextCoordinator
        return nextCoordinator.accept(
            com.caminhos2027.v1.core.model.RawGpsPosition(
                latitude = 0.0,
                longitude = 0.0,
                accuracyMeters = null,
                capturedAt = now
            )
        ).let { state ->
            // The lifecycle start position is authoritative; do not fabricate a GPS coordinate.
            state.copy(routePosition = position)
        }
    }

    fun accept(position: RawGpsPosition): WalkingState {
        val active = requireNotNull(coordinator) { "Walking session has not been started" }
        val state = active.accept(position)
        sessionService.updatePosition(state.walk.id, state)
        return state
    }

    fun markNoSignal(now: Instant): WalkingState {
        val active = requireNotNull(coordinator) { "Walking session has not been started" }
        val state = active.markNoSignal(now)
        sessionService.updatePosition(state.walk.id, state)
        return state
    }

    fun setOffline(offline: Boolean): WalkingState {
        val active = requireNotNull(coordinator) { "Walking session has not been started" }
        val state = active.setOffline(offline)
        sessionService.updatePosition(state.walk.id, state)
        return state
    }

    fun stop(position: RoutePosition, now: Instant = Instant.now()): Walk {
        val active = requireNotNull(coordinator) { "Walking session has not been started" }
        val walkId = active.state.walk.id
        val stopped = sessionService.stop(walkId, position, now)
        coordinator = null
        return stopped
    }

    fun resume(): WalkingState? {
        val walk = sessionService.resume() ?: return null
        val saved = sessionService.resumeState(walk.id)
        val nextCoordinator = WalkingStateCoordinator(route, walk, publishedApoi, policy)
        coordinator = nextCoordinator
        if (saved != null) {
            return nextCoordinator.setOffline(saved.isOffline)
                .copy(
                    routePosition = saved.routePosition,
                    gpsState = saved.gpsState,
                    progress = saved.progress,
                    nextApoi = saved.nextApoi,
                    nextApoiDistanceKm = saved.nextApoiDistanceKm
                )
        }
        return nextCoordinator.state
    }
}
