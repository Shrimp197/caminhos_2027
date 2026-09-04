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
        validateRoutePosition(position)
        val started = sessionService.start(walkId, position, now)
        val nextCoordinator = WalkingStateCoordinator(route, started, publishedApoi, policy)
        val state = nextCoordinator.seedStartPosition(position, now)
        sessionService.updatePosition(
            walkId,
            state,
            observedAt = nextCoordinator.lastReliableObservedAt()
        )
        coordinator = nextCoordinator
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
        validateRoutePosition(position)
        val stopped = sessionService.stop(active.state.walk.id, position, now)
        coordinator = null
        return stopped
    }

    /** Returns the persisted active walk without creating or replacing an in-memory coordinator. */
    fun activeWalk(): Walk? = sessionService.resume()

    /**
     * Rebuilds derived progress/APOI information from the persisted walk and checkpoint.
     * A missing persisted active session invalidates any stale in-memory coordinator.
     */
    fun resume(now: Instant = Instant.now()): WalkingState? {
        val walk = sessionService.resume()
        if (walk == null) {
            coordinator = null
            return null
        }
        require(walk.routeId == route.id) {
            "Active walking session route must match the published V1 route"
        }

        val nextCoordinator = WalkingStateCoordinator(route, walk, publishedApoi, policy)
        val checkpoint = sessionService.resumeCheckpoint(walk.id)
        val state = checkpoint?.let { nextCoordinator.restoreCheckpoint(it, now) } ?: nextCoordinator.state
        coordinator = nextCoordinator
        return state
    }

    private fun validateRoutePosition(position: RoutePosition) {
        require(position.routeId == route.id) { "Position route must match the published V1 route" }
        require(position.routeKm.isFinite() && position.routeKm >= 0.0) {
            "Position routeKm must be finite and >= 0"
        }
        require(position.routeKm <= route.totalDistanceKm) {
            "Position routeKm must not exceed the published route length"
        }
        require(position.distanceToRouteMeters.isFinite() && position.distanceToRouteMeters >= 0.0) {
            "Position distanceToRouteMeters must be finite and >= 0"
        }
    }
}
