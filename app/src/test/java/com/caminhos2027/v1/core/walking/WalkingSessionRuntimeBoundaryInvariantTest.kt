package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Boundary invariants for the persistent runtime before it can mutate lifecycle state. */
class WalkingSessionRuntimeBoundaryInvariantTest {
    @Test
    fun negativeStartRouteKmIsRejectedWithoutPersistingTheWalk() {
        val (runtime, walks) = runtime()
        runtime.prepare(WalkingPlanFactory.create(route, "negative-start", 0.0, 1.0))
        assertRejects { runtime.start("negative-start", RoutePosition(route.id, -0.001, 0.0), at("08:00:00")) }
        assertEquals(WalkStatus.PLANNED, walks.getById("negative-start")!!.status)
    }

    @Test
    fun startBeyondPublishedRouteLengthIsRejectedWithoutActivatingTheWalk() {
        val (runtime, walks) = runtime()
        runtime.prepare(WalkingPlanFactory.create(route, "past-end", 0.0, 1.0))
        assertRejects { runtime.start("past-end", RoutePosition(route.id, route.totalDistanceKm + 0.001, 0.0), at("08:00:00")) }
        assertEquals(WalkStatus.PLANNED, walks.getById("past-end")!!.status)
    }

    @Test
    fun nonFiniteDistanceToRouteIsRejectedBeforeLifecycleMutation() {
        val (runtime, walks) = runtime()
        runtime.prepare(WalkingPlanFactory.create(route, "nan-distance", 0.0, 1.0))
        assertRejects { runtime.start("nan-distance", RoutePosition(route.id, 0.2, Double.NaN), at("08:00:00")) }
        assertEquals(WalkStatus.PLANNED, walks.getById("nan-distance")!!.status)
    }

    @Test
    fun negativeDistanceToRouteIsRejectedBeforeLifecycleMutation() {
        val (runtime, walks) = runtime()
        runtime.prepare(WalkingPlanFactory.create(route, "negative-distance", 0.0, 1.0))
        assertRejects { runtime.start("negative-distance", RoutePosition(route.id, 0.2, -1.0), at("08:00:00")) }
        assertEquals(WalkStatus.PLANNED, walks.getById("negative-distance")!!.status)
    }

    @Test
    fun invalidStopBeyondRouteLengthDoesNotCompleteOrEraseCheckpoint() {
        val (runtime, walks, service) = runtimeWithService()
        runtime.prepare(WalkingPlanFactory.create(route, "invalid-stop", 0.0, 1.0))
        runtime.start("invalid-stop", RoutePosition(route.id, 0.2, 0.0), at("08:00:00"))
        val checkpoint = service.resumeCheckpoint("invalid-stop")
        assertRejects { runtime.stop(RoutePosition(route.id, route.totalDistanceKm + 0.1, 0.0), at("09:00:00")) }
        assertEquals(WalkStatus.ACTIVE, walks.getById("invalid-stop")!!.status)
        assertEquals(checkpoint, service.resumeCheckpoint("invalid-stop"))
        assertNotNull(runtime.resume(at("09:00:01")))
    }

    @Test
    fun freshCheckpointJustInsideBoundaryRestoresItsGpsState() {
        val (runtime, _, service) = runtimeWithService()
        runtime.prepare(WalkingPlanFactory.create(route, "fresh", 0.0, 1.0))
        runtime.start("fresh", RoutePosition(route.id, 0.4, 0.0), at("08:00:00"))
        runtime.accept(RawGpsPosition(40.0045, -8.0, 5.0, at("08:02:00")))
        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(at("08:02:29"))
        assertNotNull(resumed)
        assertEquals(GpsState.ON_ROUTE, resumed!!.gpsState)
        assertEquals(0.5, resumed.routePosition!!.routeKm, 0.02)
    }

    @Test
    fun checkpointExactlyAtNoSignalBoundaryRestoresAsNoSignal() {
        val (runtime, _, service) = runtimeWithService()
        runtime.prepare(WalkingPlanFactory.create(route, "boundary", 0.0, 1.0))
        runtime.start("boundary", RoutePosition(route.id, 0.4, 0.0), at("08:00:00"))
        runtime.accept(RawGpsPosition(40.0045, -8.0, 5.0, at("08:02:00")))
        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(at("08:02:30"))
        assertNotNull(resumed)
        assertEquals(GpsState.NO_SIGNAL, resumed!!.gpsState)
        assertEquals(0.5, resumed.routePosition!!.routeKm, 0.02)
    }

    @Test
    fun futureCheckpointTimestampCannotProduceFreshGpsState() {
        val (runtime, _, service) = runtimeWithService()
        runtime.prepare(WalkingPlanFactory.create(route, "future", 0.0, 1.0))
        runtime.start("future", RoutePosition(route.id, 0.4, 0.0), at("08:00:00"))
        runtime.accept(RawGpsPosition(40.0045, -8.0, 5.0, at("08:10:00")))
        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(at("08:05:00"))
        assertNotNull(resumed)
        assertEquals(GpsState.NO_SIGNAL, resumed!!.gpsState)
        assertEquals(0.5, resumed.routePosition!!.routeKm, 0.02)
    }

    private fun runtime(): Pair<WalkingSessionRuntime, InMemoryWalkRepository> {
        val walks = InMemoryWalkRepository()
        val service = WalkingSessionService(walks, InMemoryWalkingStateRepository())
        return WalkingSessionRuntime(route, service, emptyList()) to walks
    }

    private fun runtimeWithService(): Triple<WalkingSessionRuntime, InMemoryWalkRepository, WalkingSessionService> {
        val walks = InMemoryWalkRepository()
        val service = WalkingSessionService(walks, InMemoryWalkingStateRepository())
        return Triple(WalkingSessionRuntime(route, service, emptyList()), walks, service)
    }

    private fun assertRejects(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected boundary rejection.
        }
    }

    private fun at(clock: String) = Instant.parse("2026-09-04T${clock}Z")

    companion object {
        private val route = Route(
            "runtime-boundary-route", "TEST/FICTITIOUS route", "TEST/FICTITIOUS route", 1.0,
            "TEST/FICTITIOUS", "2026-09-04",
            RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0045, -8.0), GeoPoint(40.009, -8.0))),
            emptyList()
        )
    }
}
