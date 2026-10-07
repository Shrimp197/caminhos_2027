package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.WalkStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class WalkingSessionRuntimeTerminalStateTest {
    private val route = Route(
        id = "route",
        name = "Synthetic route",
        officialName = "Synthetic route",
        totalDistanceKm = 2.0,
        source = "TEST",
        updatedAt = "2026-09-04",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0))),
        stages = emptyList()
    )

    private val start = RoutePosition("route", 0.4, 0.0, null, PositionConfidence.HIGH)
    private val end = RoutePosition("route", 1.4, 0.0, null, PositionConfidence.HIGH)

    @Test
    fun completedRuntimeCannotBeStoppedOrResumedAgain() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-terminal", 0.4, 1.8))
        runtime.start("walk-terminal", start, Instant.parse("2026-09-04T10:00:00Z"))

        val completed = runtime.stop(end, Instant.parse("2026-09-04T10:01:00Z"))
        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertNull(runtime.resume())

        try {
            runtime.stop(end, Instant.parse("2026-09-04T10:02:00Z"))
            fail("Expected terminal runtime to reject a second stop")
        } catch (_: IllegalArgumentException) {
            // Terminal state must not be resurrected.
        }

        assertEquals(WalkStatus.COMPLETED, service.get("walk-terminal")!!.status)
        assertNull(states.get("walk-terminal"))
    }

    @Test
    fun rejectedStopWithWrongRouteDoesNotMutateActiveSession() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-route", 0.4, 1.8))
        runtime.start("walk-route", start, Instant.parse("2026-09-04T11:00:00Z"))
        val checkpointBefore = states.get("walk-route")

        try {
            runtime.stop(RoutePosition("other", 1.4, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T11:01:00Z"))
            fail("Expected route mismatch to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }

        assertEquals(WalkStatus.ACTIVE, service.resume()!!.status)
        assertEquals(checkpointBefore, states.get("walk-route"))
        assertEquals(WalkStatus.COMPLETED, runtime.stop(end, Instant.parse("2026-09-04T11:02:00Z")).status)
    }
}
