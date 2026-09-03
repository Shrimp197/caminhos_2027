package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WalkingSessionRuntimeTest {
    private val route = Route("sr-route", "SR", "SR synthetic route", 2.0, "SR", "2026-09-01", RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))), listOf(
        Stage("stage-1", "sr-route", 1, "Stage 1", 0.0, 1.0, 1.0, "A", "B", "SR"),
        Stage("stage-2", "sr-route", 2, "Stage 2", 1.0, 2.0, 1.0, "B", "C", "SR")
    ))
    private val start = RoutePosition("sr-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH)
    private val stop = RoutePosition("sr-route", 1.4, 3.0, "stage-2", PositionConfidence.HIGH)

    @Test fun startAcceptStopCompletesLifecycleWithoutFabricatedGps() {
        val walks = InMemoryWalkRepository(); val states = InMemoryWalkingStateRepository()
        val runtime = WalkingSessionRuntime(route, WalkingSessionService(walks, states), emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-1", 0.4, 1.8))
        val started = runtime.start("walk-1", start, Instant.parse("2026-09-01T08:00:00Z"))
        assertEquals(WalkStatus.ACTIVE, started.walk.status); assertEquals(0.4, started.routePosition!!.routeKm, 0.001); assertEquals(GpsState.ACQUIRING, started.gpsState)
        val completed = runtime.stop(stop, Instant.parse("2026-09-01T12:00:00Z"))
        assertEquals(WalkStatus.COMPLETED, completed.status); assertEquals(1.4, completed.actualEndKm!!, 0.001); assertNull(states.get("walk-1"))
    }

    @Test fun firstImplausibleFixDoesNotReplaceTheStartBaseline() {
        val walks = InMemoryWalkRepository(); val states = InMemoryWalkingStateRepository(); val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-3", 0.4, 1.8))
        val started = runtime.start("walk-3", start, Instant.parse("2026-09-01T08:00:00Z"))

        val afterJump = runtime.accept(RawGpsPosition(40.018, -8.0, 5.0, Instant.parse("2026-09-01T08:01:00Z")))

        assertEquals(0.4, afterJump.routePosition!!.routeKm, 0.001)
        assertEquals(start.routeKm, afterJump.progress!!.currentRouteKm, 0.001)
        assertEquals(started.progress!!.walkedKm, afterJump.progress.walkedKm, 0.001)
        assertEquals(GpsState.ACQUIRING, afterJump.gpsState)
    }

    @Test fun resumeReturnsTheLastCheckpointedState() {
        val walks = InMemoryWalkRepository(); val states = InMemoryWalkingStateRepository(); val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList()); runtime.prepare(WalkingPlanFactory.create(route, "walk-2", 0.4, 1.8)); runtime.start("walk-2", start, Instant.parse("2026-09-01T08:00:00Z"))
        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(Instant.parse("2026-09-01T08:05:00Z"))
        assertNotNull(resumed); assertEquals(0.4, resumed!!.routePosition!!.routeKm, 0.001); assertEquals(GpsState.ACQUIRING, resumed.gpsState)
    }

    @Test fun checkpointPreservesLastAcceptedGpsTimestampAcrossRuntimeRecreation() {
        val walks = InMemoryWalkRepository(); val states = InMemoryWalkingStateRepository(); val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-5", 0.4, 1.8))
        runtime.start("walk-5", start, Instant.parse("2026-09-01T08:00:00Z"))

        runtime.accept(RawGpsPosition(40.0045, -8.0, 5.0, Instant.parse("2026-09-01T08:02:00Z")))
        val checkpoint = service.resumeCheckpoint("walk-5")

        assertNotNull(checkpoint)
        assertEquals(Instant.parse("2026-09-01T08:02:00Z"), checkpoint!!.lastObservedAt)

        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(Instant.parse("2026-09-01T08:20:00Z"))
        assertNotNull(resumed)
        assertEquals(0.5, resumed!!.routePosition!!.routeKm, 0.02)
    }

    @Test fun resumedRuntimeStillRejectsAnImplausibleGpsJumpFromCheckpoint() {
        val walks = InMemoryWalkRepository(); val states = InMemoryWalkingStateRepository(); val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-4", 0.4, 1.8))
        runtime.start("walk-4", start, Instant.parse("2026-09-01T08:00:00Z"))

        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(Instant.parse("2026-09-01T08:05:00Z"))!!
        assertEquals(0.4, resumed.routePosition!!.routeKm, 0.001)

        WalkingSessionRuntime(route, service, emptyList()).apply {
            resume(Instant.parse("2026-09-01T08:05:00Z"))
            accept(RawGpsPosition(40.018, -8.0, 5.0, Instant.parse("2026-09-01T08:06:00Z")))
        }
        val afterJump = WalkingSessionRuntime(route, service, emptyList()).resume(Instant.parse("2026-09-01T08:07:00Z"))

        assertNotNull(afterJump)
        assertEquals(0.4, afterJump!!.routePosition!!.routeKm, 0.001)
    }
}
