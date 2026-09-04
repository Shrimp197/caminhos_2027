package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
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
    private val publishedApoi: List<Apoi>,
    private val policy: GpsTrackingPolicy = GpsTrackingPolicy()
) {
    private var coordinator: WalkingStateCoordinator? = null

    fun prepare(walk: Walk): Walk = sessionService.prepare(walk)

    fun start(walkId: String, position: RoutePosition, now: Instant = Instant.now()): WalkingState {
        val started = sessionService.start(walkId, position, now)
        val nextCoordinator = WalkingStateCoordinator(route, started, publishedApoi, policy)
        coordinator = nextCoordinator
        val state = nextCoordinator.seedStartPosition(position, now)
        sessionService.updatePosition(walkId, state, observedAt = now)
        return state
    }

    fun accept(position: RawGpsPosition): WalkingState {
        val active = requireNotNull(coordinator) { "Walking session has not been started" }
        val state = active.accept(position)
        sessionService.updatePosition(
            state.walk.id,
            state,
            observedAt = active.lastReliableObservedAt()
        )
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
        val stopped = sessionService.stop(active.state.walk.id, position, now)
        coordinator = null
        return stopped
    }

    /** Rebuilds derived progress/APOI information from the persisted walk and checkpoint. */
    fun resume(now: Instant = Instant.now()): WalkingState? {
        val walk = sessionService.resume() ?: return null
        val nextCoordinator = WalkingStateCoordinator(route, walk, publishedApoi, policy)
        coordinator = nextCoordinator
        val checkpoint = sessionService.resumeCheckpoint(walk.id) ?: return nextCoordinator.state
        return nextCoordinator.restoreCheckpoint(checkpoint, now)
    }
}
