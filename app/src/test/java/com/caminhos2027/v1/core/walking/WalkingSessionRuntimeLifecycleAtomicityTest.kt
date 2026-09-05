package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.WalkStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Test

class WalkingSessionRuntimeLifecycleAtomicityTest {
    private val route = Route(
        id = "route",
        name = "Synthetic route",
        officialName = "Synthetic route",
        totalDistanceKm = 2.0,
        source = "TEST",
        updatedAt = "2026-09-04",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))),
        stages = emptyList()
    )

    private val start = RoutePosition("route", 0.4, 0.0, null, PositionConfidence.HIGH)
    private val end = RoutePosition("route", 1.4, 0.0, null, PositionConfidence.HIGH)

    @Test
    fun failedStopDoesNotDestroyActiveRuntimeCoordinator() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk", 0.4, 1.8))
        runtime.start("walk", start, Instant.parse("2026-09-04T10:00:00Z"))

        try {
            runtime.stop(end, Instant.parse("2026-09-04T09:59:59Z"))
            fail("Expected stop before start to be rejected")
        } catch (_: IllegalArgumentException) {
            // The active runtime must remain usable after the rejected mutation.
        }

        val completed = runtime.stop(end, Instant.parse("2026-09-04T10:00:01Z"))

        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertEquals(1.4, completed.actualEndKm!!, 0.001)
    }

    @Test
    fun failedStopLeavesPersistedWalkActiveAndCheckpointIntact() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-persist", 0.4, 1.8))
        runtime.start("walk-persist", start, Instant.parse("2026-09-04T10:00:00Z"))
        val before = service.resume()

        try {
            runtime.stop(end, Instant.parse("2026-09-04T09:59:59Z"))
            fail("Expected stop before start to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }

        val after = service.resume()
        assertNotNull(before)
        assertNotNull(after)
        assertEquals(WalkStatus.ACTIVE, after!!.status)
        assertEquals(before!!.id, after.id)
        assertEquals(0.4, states.get("walk-persist")!!.routePosition!!.routeKm, 0.001)
    }

    @Test
    fun invalidStopPositionDoesNotClearTheCheckpoint() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-invalid", 0.4, 1.8))
        runtime.start("walk-invalid", start, Instant.parse("2026-09-04T10:00:00Z"))
        val checkpointBefore = states.get("walk-invalid")

        try {
            runtime.stop(RoutePosition("route", 2.1, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T10:01:00Z"))
            fail("Expected position beyond published route to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }

        assertEquals(checkpointBefore, states.get("walk-invalid"))
        assertEquals(WalkStatus.ACTIVE, service.resume()!!.status)

        val completed = runtime.stop(end, Instant.parse("2026-09-04T10:02:00Z"))
        assertEquals(WalkStatus.COMPLETED, completed.status)
    }
}
