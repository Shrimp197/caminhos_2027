package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

class WalkingSessionRuntimePersistenceInvariantTest {
    private val route = Route(
        "persist-route",
        "PERSIST",
        "PERSIST synthetic route",
        2.0,
        "TEST",
        "2026-09-04",
        RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))),
        listOf(
            Stage("stage-1", "persist-route", 1, "Stage 1", 0.0, 1.0, 1.0, "A", "B", "TEST"),
            Stage("stage-2", "persist-route", 2, "Stage 2", 1.0, 2.0, 1.0, "B", "C", "TEST")
        )
    )
    private val start = RoutePosition("persist-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH)
    private val firstFix = RawGpsPosition(40.0045, -8.0, 5.0, Instant.parse("2026-09-04T08:02:00Z"))

    @Test
    fun outOfOrderGpsCannotOverwritePersistedCheckpoint() {
        val (runtime, service) = runtime("walk-order")
        runtime.start("walk-order", start, Instant.parse("2026-09-04T08:00:00Z"))
        runtime.accept(firstFix)
        val before = service.resumeCheckpoint("walk-order")!!

        runtime.accept(firstFix.copy(latitude = 40.0018, capturedAt = Instant.parse("2026-09-04T08:01:00Z")))

        val after = service.resumeCheckpoint("walk-order")!!
        assertEquals(before.routePosition, after.routePosition)
        assertEquals(before.lastObservedAt, after.lastObservedAt)
        assertEquals(GpsState.ON_ROUTE, after.gpsState)
    }

    @Test
    fun rejectedTemporalObservationLeavesReliableBaselineAndTimestampUntouched() {
        val (runtime, service) = runtime("walk-temporal")
        runtime.start("walk-temporal", start, Instant.parse("2026-09-04T08:00:00Z"))
        runtime.accept(firstFix)
        val before = service.resumeCheckpoint("walk-temporal")!!

        runtime.accept(firstFix.copy(latitude = 40.018, capturedAt = Instant.parse("2026-09-04T08:01:00Z")))
        val after = service.resumeCheckpoint("walk-temporal")!!

        assertEquals(before.routePosition, after.routePosition)
        assertEquals(before.lastObservedAt, after.lastObservedAt)
    }

    @Test
    fun signalLossDoesNotEraseLastReliableCheckpoint() {
        val (runtime, service) = runtime("walk-signal")
        runtime.start("walk-signal", start, Instant.parse("2026-09-04T08:00:00Z"))
        val moved = runtime.accept(firstFix)

        val noSignal = runtime.markNoSignal(Instant.parse("2026-09-04T08:02:31Z"))
        val checkpoint = service.resumeCheckpoint("walk-signal")!!

        assertEquals(GpsState.NO_SIGNAL, noSignal.gpsState)
        assertEquals(moved.routePosition, checkpoint.routePosition)
        assertEquals(firstFix.capturedAt, checkpoint.lastObservedAt)
    }

    @Test
    fun runtimeRecreationKeepsCheckpointAfterRejectedObservation() {
        val (runtime, service) = runtime("walk-recreate")
        runtime.start("walk-recreate", start, Instant.parse("2026-09-04T08:00:00Z"))
        runtime.accept(firstFix)
        runtime.accept(firstFix.copy(latitude = 40.018, capturedAt = Instant.parse("2026-09-04T08:01:00Z")))

        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(Instant.parse("2026-09-04T08:02:31Z"))

        assertNotNull(resumed)
        assertEquals(0.5, resumed!!.routePosition!!.routeKm, 0.02)
        assertEquals(GpsState.NO_SIGNAL, resumed.gpsState)
        assertEquals(firstFix.capturedAt, service.resumeCheckpoint("walk-recreate")!!.lastObservedAt)
    }

    private fun runtime(walkId: String): Pair<WalkingSessionRuntime, WalkingSessionService> {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, walkId, 0.4, 1.8))
        return runtime to service
    }
}
