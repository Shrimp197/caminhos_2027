package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import java.time.Instant

/** Pure lifecycle rules for a walking session. Persistence and Android location remain outside this class. */
object WalkingSessionController {
    fun start(walk: Walk, startPosition: RoutePosition, now: Instant = Instant.now()): Walk {
        require(walk.status == WalkStatus.PLANNED) { "Only a planned walk can be started" }
        require(walk.routeId == startPosition.routeId) { "Walk and position route must match" }
        requirePosition(startPosition)
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
        requirePosition(endPosition)
        require(walk.startedAt == null || !now.isBefore(walk.startedAt)) {
            "Walk end time cannot be before walk start time"
        }
        return walk.copy(
            actualEndKm = endPosition.routeKm,
            endedAt = now,
            status = WalkStatus.COMPLETED
        )
    }

    fun canResume(walk: Walk): Boolean = walk.status == WalkStatus.ACTIVE

    private fun requirePosition(position: RoutePosition) {
        require(position.routeId.isNotBlank()) { "Position routeId must not be blank" }
        require(position.routeKm.isFinite() && position.routeKm >= 0.0) {
            "Position routeKm must be finite and >= 0"
        }
        require(position.distanceToRouteMeters.isFinite() && position.distanceToRouteMeters >= 0.0) {
            "Position distanceToRouteMeters must be finite and >= 0"
        }
    }
}
