package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import java.time.Instant

/** Coordinates walking lifecycle and persistence without depending on Android or UI. */
class WalkingSessionService(
    private val repository: WalkRepository
) {
    fun prepare(walk: Walk): Walk {
        require(walk.status == com.caminhos2027.v1.core.model.WalkStatus.PLANNED) {
            "Only a planned walk can be prepared"
        }
        repository.save(walk)
        return walk
    }

    fun start(walkId: String, startPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        val walk = requireWalk(walkId)
        val started = WalkingSessionController.start(walk, startPosition, now)
        repository.save(started)
        return started
    }

    fun updatePosition(walkId: String, position: RoutePosition): Walk {
        val walk = requireWalk(walkId)
        require(walk.status == com.caminhos2027.v1.core.model.WalkStatus.ACTIVE) {
            "Only an active walk can be updated"
        }
        require(walk.routeId == position.routeId) { "Walk and position route must match" }
        // Position is intentionally not persisted in Walk yet. RoutePosition belongs to the
        // current walking state; a later persistence adapter can checkpoint it independently.
        return walk
    }

    fun stop(walkId: String, endPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        val walk = requireWalk(walkId)
        val stopped = WalkingSessionController.stop(walk, endPosition, now)
        repository.save(stopped)
        return stopped
    }

    fun resume(): Walk? = repository.getActive()

    fun get(walkId: String): Walk? = repository.getById(walkId)

    private fun requireWalk(walkId: String): Walk =
        requireNotNull(repository.getById(walkId)) { "Walk not found: $walkId" }
}
