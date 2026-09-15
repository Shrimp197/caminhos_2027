package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingSessionCheckpointRuntimeTest {
    private val repository = InMemoryWalkRepository()
    private val stateRepository = InMemoryWalkingStateRepository()
    private val sessionService = WalkingSessionService(repository, stateRepository)
    private val route = testRoute()

    @Test
    fun resumeRebuildsDerivedStateFromPersistedCheckpoint() {
        val runtime = WalkingSessionRuntime(route, sessionService, emptyList())
        val walk = Walk(
            id = "runtime-walk-1",
            routeId = route.id,
            plannedStartKm = 0.0,
            plannedDestinationKm = 1.0
        )
        runtime.prepare(walk)

        runtime.start(
            walk.id,
            RoutePosition(route.id, 0.0, 0.0),
            Instant.parse("2026-09-03T08:00:00Z")
        )
        val beforeResume = runtime.accept(
            RawGpsPosition(
                latitude = 0.0045,
                longitude = 0.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-03T08:10:00Z")
            )
        )

        val restoredRuntime = WalkingSessionRuntime(route, sessionService, emptyList())
        val restored = restoredRuntime.resume(Instant.parse("2026-09-03T08:10:05Z"))

        assertNotNull(restored)
        assertEquals(WalkStatus.ACTIVE, restored!!.walk.status)
        assertEquals(beforeResume.routePosition!!.routeKm, restored.routePosition!!.routeKm, 0.001)
        assertEquals(beforeResume.progress!!.walkedKm, restored.progress!!.walkedKm, 0.001)
        assertEquals(beforeResume.progress.remainingKm, restored.progress.remainingKm, 0.001)
        assertEquals(beforeResume.gpsState, restored.gpsState)
    }

    @Test
    fun stoppingRuntimeClearsPersistedCheckpoint() {
        val runtime = WalkingSessionRuntime(route, sessionService, emptyList())
        val walk = Walk("runtime-walk-2", route.id, plannedDestinationKm = 1.0)
        runtime.prepare(walk)
        runtime.start(walk.id, RoutePosition(route.id, 0.0, 0.0), Instant.parse("2026-09-03T08:00:00Z"))

        runtime.stop(RoutePosition(route.id, 1.0, 0.0), Instant.parse("2026-09-03T09:00:00Z"))

        assertNull(stateRepository.get(walk.id))
        assertEquals(WalkStatus.COMPLETED, repository.getById(walk.id)!!.status)
    }

    private fun testRoute() = Route(
        id = "runtime-test-route",
        name = "Runtime test route",
        officialName = "Runtime test route official",
        totalDistanceKm = 1.0,
        source = "runtime-test",
        updatedAt = "2026-09-03",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(0.0, 0.0),
                GeoPoint(0.0045, 0.0),
                GeoPoint(0.0090, 0.0)
            )
        ),
        stages = listOf(
            Stage("runtime-stage", "runtime-test-route", 1, "Runtime stage", 0.0, 1.0, 1.0, "Start", "End", "runtime-test")
        )
    )
}
