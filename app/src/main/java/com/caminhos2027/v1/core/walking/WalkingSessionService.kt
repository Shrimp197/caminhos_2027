package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
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
        val started = WalkingSessionController.start(walk, startPosition, now)
        repository.save(started)
        stateRepository?.save(walkId, WalkingCheckpoint(startPosition, com.caminhos2027.v1.core.route.GpsState.ACQUIRING, false))
        return started
    }

    /** Validates and checkpoints only device-derived state; it never changes the walking plan. */
    fun updatePosition(walkId: String, state: WalkingState): WalkingState {
        val walk = requireWalk(walkId)
        require(walk.status == WalkStatus.ACTIVE) { "Only an active walk can be updated" }
        require(walk.id == state.walk.id) { "Walking state walk must match requested walk" }
        state.routePosition?.let { position ->
            require(walk.routeId == position.routeId) { "Walk and position route must match" }
        }
        stateRepository?.save(
            walkId,
            WalkingCheckpoint(state.routePosition, state.gpsState, state.isOffline)
        )
        return state
    }

    fun stop(walkId: String, endPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        val walk = requireWalk(walkId)
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
}
