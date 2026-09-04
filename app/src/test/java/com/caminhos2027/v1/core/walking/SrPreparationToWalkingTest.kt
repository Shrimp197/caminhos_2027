package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** SR vertical slice: preparation -> persistence -> start -> resume -> stop. */
class SrPreparationToWalkingTest {
    private val route = Route(
        "sr-route", "SR", "SR synthetic route", 2.0, "SR", "2026-09-01",
        RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))),
        listOf(
            Stage("stage-1", "sr-route", 1, "Stage 1", 0.0, 1.0, 1.0, "A", "B", "SR"),
            Stage("stage-2", "sr-route", 2, "Stage 2", 1.0, 2.0, 1.0, "B", "C", "SR")
        )
    )

    @Test
    fun preparedPlanCanBeStartedResumedAndCompleted() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList<Apoi>() }))
        val preparationService = WalkingPreparationService(route, walks, catalog)
        val preparation = preparationService.save("sr-vertical", 0.4, 1.8)

        assertEquals(WalkStatus.PLANNED, preparation.walk.status)
        assertEquals(2, preparation.stages.size)

        val runtime = WalkingSessionRuntime(route, service, emptyList())
        val started = runtime.start(
            preparation.walk.id,
            RoutePosition("sr-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-01T08:00:00Z")
        )
        assertEquals(WalkStatus.ACTIVE, started.walk.status)
        assertEquals(GpsState.ACQUIRING, started.gpsState)

        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume()
        assertNotNull(resumed)
        assertEquals(0.4, resumed!!.routePosition!!.routeKm, 0.001)

        val completed = WalkingSessionRuntime(route, service, emptyList()).let { activeRuntime ->
            activeRuntime.resume()
            activeRuntime.stop(
                RoutePosition("sr-route", 1.4, 3.0, "stage-2", PositionConfidence.HIGH),
                Instant.parse("2026-09-01T12:00:00Z")
            )
        }
        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertEquals(1.4, completed.actualEndKm!!, 0.001)
    }

    @Test
    fun plannedWalkIsNotResumableBeforeItStarts() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val preparation = WalkingPreparationService(
            route,
            walks,
            emptyCatalog()
        ).save("sr-planned", 0.2, 1.2)

        assertEquals(WalkStatus.PLANNED, preparation.walk.status)
        assertNull(WalkingSessionRuntime(route, service, emptyList()).resume())
        assertNull(states.get(preparation.walk.id))
    }

    @Test
    fun rejectedStartPositionDoesNotActivatePreparedWalk() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val preparation = WalkingPreparationService(route, walks, emptyCatalog()).save("sr-start-boundary", 0.4, 1.8)
        val runtime = WalkingSessionRuntime(route, service, emptyList())

        try {
            runtime.start(
                preparation.walk.id,
                RoutePosition("foreign-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH),
                Instant.parse("2026-09-01T08:00:00Z")
            )
            throw AssertionError("Expected foreign route start to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected boundary rejection.
        }

        assertEquals(WalkStatus.PLANNED, walks.getById(preparation.walk.id)!!.status)
        assertNull(states.get(preparation.walk.id))
    }

    @Test
    fun rejectedStopLeavesActiveCheckpointAndWalkUntouched() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val preparation = WalkingPreparationService(route, walks, emptyCatalog()).save("sr-stop-boundary", 0.4, 1.8)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.start(
            preparation.walk.id,
            RoutePosition("sr-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-01T08:00:00Z")
        )
        val checkpointBefore = states.get(preparation.walk.id)
        val walkBefore = walks.getById(preparation.walk.id)

        try {
            runtime.stop(
                RoutePosition("foreign-route", 1.0, 3.0, "stage-2", PositionConfidence.HIGH),
                Instant.parse("2026-09-01T12:00:00Z")
            )
            throw AssertionError("Expected foreign route stop to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected boundary rejection.
        }

        assertEquals(WalkStatus.ACTIVE, walks.getById(preparation.walk.id)!!.status)
        assertEquals(walkBefore, walks.getById(preparation.walk.id))
        assertEquals(checkpointBefore, states.get(preparation.walk.id))
    }

    @Test
    fun completionClearsCheckpointAndCannotBeResumed() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val preparation = WalkingPreparationService(route, walks, emptyCatalog()).save("sr-terminal", 0.4, 1.8)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.start(
            preparation.walk.id,
            RoutePosition("sr-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-01T08:00:00Z")
        )

        val completed = runtime.stop(
            RoutePosition("sr-route", 1.4, 3.0, "stage-2", PositionConfidence.HIGH),
            Instant.parse("2026-09-01T12:00:00Z")
        )

        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertNull(states.get(preparation.walk.id))
        assertNull(WalkingSessionRuntime(route, service, emptyList()).resume())
    }

    @Test
    fun resumedRuntimeCanContinueFromPersistedCheckpoint() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val preparation = WalkingPreparationService(route, walks, emptyCatalog()).save("sr-recreate", 0.4, 1.8)
        val firstRuntime = WalkingSessionRuntime(route, service, emptyList())
        firstRuntime.start(
            preparation.walk.id,
            RoutePosition("sr-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-01T08:00:00Z")
        )
        firstRuntime.accept(
            com.caminhos2027.v1.core.model.RawGpsPosition(
                latitude = 40.0045,
                longitude = -8.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-01T08:01:00Z")
            )
        )

        val recreated = WalkingSessionRuntime(route, service, emptyList())
        val resumed = recreated.resume(Instant.parse("2026-09-01T08:01:10Z"))

        assertNotNull(resumed)
        assertEquals(WalkStatus.ACTIVE, resumed!!.walk.status)
        assertNotNull(resumed.routePosition)
        assertTrue(resumed.routePosition!!.routeKm >= 0.4)
        assertFalse(resumed.isOffline)
    }

    private fun emptyCatalog() = PublishedApoiCatalog(
        ApoiRepository(ApoiDataSource { emptyList<Apoi>() })
    )
}
