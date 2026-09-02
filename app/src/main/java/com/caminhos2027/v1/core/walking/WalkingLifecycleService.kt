package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk

/** Coordinates lifecycle transitions with persistence without depending on Android. */
class WalkingLifecycleService(private val repository: WalkRepository) {
    fun prepare(walk: Walk): Walk {
        require(repository.getById(walk.id) == null) { "A walk with this id already exists" }
        repository.save(walk)
        return walk
    }

    fun start(walkId: String, position: RoutePosition, now: java.time.Instant): Walk {
        val walk = requireNotNull(repository.getById(walkId)) { "Walk not found" }
        val updated = WalkingSessionController.start(walk, position, now)
        repository.save(updated)
        return updated
    }

    fun stop(walkId: String, position: RoutePosition, now: java.time.Instant): Walk {
        val walk = requireNotNull(repository.getById(walkId)) { "Walk not found" }
        val updated = WalkingSessionController.stop(walk, position, now)
        repository.save(updated)
        return updated
    }

    fun activeWalk(): Walk? = repository.getActive()

    fun resume(walkId: String): Walk {
        val walk = requireNotNull(repository.getById(walkId)) { "Walk not found" }
        require(WalkingSessionController.canResume(walk)) { "Only an active walk can be resumed" }
        return walk
    }
}
