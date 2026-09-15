package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant

/** Minimal persistence boundary for the latest device-derived walking checkpoint. */
data class WalkingCheckpoint(
    val routePosition: RoutePosition?,
    val gpsState: GpsState,
    val isOffline: Boolean,
    /** Capture time of the last accepted GPS observation used as continuity baseline. */
    val lastObservedAt: Instant? = null
)

interface WalkingStateRepository {
    fun save(walkId: String, checkpoint: WalkingCheckpoint)
    fun get(walkId: String): WalkingCheckpoint?
    fun clear(walkId: String)
}

/** Deterministic repository used by domain tests and early integration work. */
class InMemoryWalkingStateRepository : WalkingStateRepository {
    private val states = linkedMapOf<String, WalkingCheckpoint>()

    override fun save(walkId: String, checkpoint: WalkingCheckpoint) {
        states[walkId] = checkpoint
    }

    override fun get(walkId: String): WalkingCheckpoint? = states[walkId]

    override fun clear(walkId: String) {
        states.remove(walkId)
    }
}
