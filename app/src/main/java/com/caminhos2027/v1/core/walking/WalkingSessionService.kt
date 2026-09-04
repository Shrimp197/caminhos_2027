package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant

/** Coordinates walking lifecycle and minimal current-position checkpointing. */
class WalkingSessionService(
    private val repository: WalkRepository,
    private val stateRepository: WalkingStateRepository? = null
) {
    fun prepare(walk: Walk): Walk {
        require(walk.status == WalkStatus.PLANNED) { "Only a planned walk can be prepared" }
        repository.save(walk)
        return walk
    }

    fun start(walkId: String, startPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        val walk = requireWalk(walkId)
        validatePosition(walk, startPosition)
        val started = WalkingSessionController.start(walk, startPosition, now)
        repository.save(started)
        stateRepository?.save(
            walkId,
            WalkingCheckpoint(startPosition, GpsState.ACQUIRING, false, lastObservedAt = null)
        )
        return started
    }

    /** Validates and checkpoints only device-derived state; it never changes the walking plan. */
    fun updatePosition(
        walkId: String,
        state: WalkingState,
        observedAt: Instant? = null
    ): WalkingState {
        val walk = requireWalk(walkId)
        require(walk.status == WalkStatus.ACTIVE) { "Only an active walk can be updated" }
        require(walk.id == state.walk.id) { "Walking state walk must match requested walk" }
        state.routePosition?.let { validatePosition(walk, it) }
        val previousObservedAt = stateRepository?.get(walkId)?.lastObservedAt
        require(
            observedAt == null || previousObservedAt == null || !observedAt.isBefore(previousObservedAt)
        ) { "Observed GPS time cannot move backwards" }
        stateRepository?.save(
            walkId,
            WalkingCheckpoint(
                state.routePosition,
                state.gpsState,
                state.isOffline,
                lastObservedAt = observedAt ?: previousObservedAt
            )
        )
        return state
    }

    fun stop(walkId: String, endPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        val walk = requireWalk(walkId)
        validatePosition(walk, endPosition)
        val stopped = WalkingSessionController.stop(walk, endPosition, now)
        repository.save(stopped)
        stateRepository?.clear(walkId)
        return stopped
    }

    fun resume(): Walk? = repository.getActive()

    fun resumeCheckpoint(walkId: String): WalkingCheckpoint? = stateRepository?.get(walkId)

    fun get(walkId: String): Walk? = repository.getById(walkId)

    private fun requireWalk(walkId: String): Walk =
        requireNotNull(repository.getById(walkId)) { "Walk not found: $walkId" }

    private fun validatePosition(walk: Walk, position: RoutePosition) {
        require(walk.routeId == position.routeId) { "Walk and position route must match" }
        require(position.routeKm.isFinite() && position.routeKm >= 0.0) {
            "Position routeKm must be finite and >= 0"
        }
        require(position.distanceToRouteMeters.isFinite() && position.distanceToRouteMeters >= 0.0) {
            "Position distanceToRouteMeters must be finite and >= 0"
        }
    }
}
