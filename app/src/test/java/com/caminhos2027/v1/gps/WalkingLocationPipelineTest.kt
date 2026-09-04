package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingLocationPipelineTest {
    private val route = Route(
        id = "test-route",
        name = "Teste",
        officialName = "Teste",
        geometry = RouteGeometry(listOf(
            GeoPoint(41.0, -8.0),
            GeoPoint(41.01, -8.0),
            GeoPoint(41.02, -8.0)
        )),
        totalDistanceKm = 2.22,
        source = "test",
        updatedAt = "2026-09-01",
        stages = emptyList()
    )

    @Test
    fun acceptsRawPositionAndProjectsItToRoute() {
        val now = Instant.parse("2026-09-01T10:00:05Z")
        val pipeline = WalkingLocationPipeline(route, clock = { now })
        val state = pipeline.accept(RawGpsPosition(41.005, -8.0, 5.0, Instant.parse("2026-09-01T10:00:00Z")))

        assertEquals(GpsState.ON_ROUTE, state.state)
        assertTrue(state.lastReliableObservation != null)
        assertEquals("test-route", state.lastReliableObservation!!.routePosition.routeId)
    }

    @Test
    fun materiallyFutureObservationIsIgnoredByAndroidPipeline() {
        val now = Instant.parse("2026-09-01T10:00:00Z")
        val pipeline = WalkingLocationPipeline(route, clock = { now })
        val future = now.plusSeconds(16)

        val state = pipeline.accept(RawGpsPosition(41.005, -8.0, 5.0, future))

        assertEquals(GpsState.NO_SIGNAL, state.state)
        assertEquals(null, state.lastObservation)
        assertEquals(null, state.lastReliableObservation)
    }

    @Test
    fun smallClockSkewIsAcceptedWithinPolicy() {
        val now = Instant.parse("2026-09-01T10:00:00Z")
        val pipeline = WalkingLocationPipeline(route, clock = { now })
        val slightlyFuture = now.plusSeconds(10)

        val state = pipeline.accept(RawGpsPosition(41.005, -8.0, 5.0, slightlyFuture))

        assertEquals(GpsState.ON_ROUTE, state.state)
        assertEquals(slightlyFuture, state.lastReliableObservation?.capturedAt)
    }

    @Test
    fun provisionalSeedDoesNotBecomeReliableUntilGpsObservationIsAccepted() {
        val capturedAt = Instant.parse("2026-09-01T10:00:00Z")
        val provisional = RoutePosition("test-route", 0.2, 150.0, null, PositionConfidence.LOW)
        val pipeline = WalkingLocationPipeline(route)

        val seeded = pipeline.seedRoutePosition(provisional, capturedAt, reliable = false)
        assertEquals(null, seeded.lastReliableObservation)
        assertEquals(provisional, seeded.lastObservation?.routePosition)

        val accepted = pipeline.accept(RawGpsPosition(41.002, -8.0, 5.0, capturedAt.plusSeconds(1)))
        assertEquals(GpsState.ON_ROUTE, accepted.state)
        assertEquals(capturedAt.plusSeconds(1), accepted.lastReliableObservation?.capturedAt)
    }

    @Test
    fun prolongedMissingUpdatesBecomeNoSignal() {
        val first = Instant.parse("2026-09-01T10:00:00Z")
        val pipeline = WalkingLocationPipeline(route, clock = { first })
        pipeline.accept(RawGpsPosition(41.005, -8.0, 5.0, first))

        val state = pipeline.markNoSignal(first.plusSeconds(31))

        assertEquals(GpsState.NO_SIGNAL, state.state)
        assertTrue(state.lastReliableObservation != null)
    }
}
