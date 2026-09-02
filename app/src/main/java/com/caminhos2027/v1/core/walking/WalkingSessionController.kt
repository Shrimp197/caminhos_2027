package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant

/** Pure lifecycle rules for a walking session. Persistence and Android location remain outside this class. */
object WalkingSessionController {
    fun start(walk: Walk, startPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        require(walk.status == WalkStatus.PLANNED) { "Only a planned walk can be started" }
        require(walk.routeId == startPosition.routeId) { "Walk and position route must match" }
        return walk.copy(
            actualStartKm = startPosition.routeKm,
            actualEndKm = null,
            startedAt = now,
            endedAt = null,
            status = WalkStatus.ACTIVE
        )
    }

    fun stop(walk: Walk, endPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        require(walk.status == WalkStatus.ACTIVE) { "Only an active walk can be stopped" }
        require(walk.routeId == endPosition.routeId) { "Walk and position route must match" }
        return walk.copy(
            actualEndKm = endPosition.routeKm,
            endedAt = now,
            status = WalkStatus.COMPLETED
        )
    }

    fun canResume(walk: Walk): Boolean = walk.status == WalkStatus.ACTIVE

    fun effectiveGpsState(state: GpsState): GpsState = state

    fun routePositionForProgress(position: RoutePosition?): Double? = position?.routeKm
}
