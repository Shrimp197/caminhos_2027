package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
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
        source = "test"
    )

    @Test
    fun acceptsRawPositionAndProjectsItToRoute() {
        val pipeline = WalkingLocationPipeline(route)
        val state = pipeline.accept(RawGpsPosition(41.005, -8.0, 5.0, Instant.parse("2026-09-01T10:00:00Z")))

        assertEquals(GpsState.ON_ROUTE, state.state)
        assertTrue(state.lastReliableObservation != null)
        assertEquals("test-route", state.lastReliableObservation!!.routePosition.routeId)
    }

    @Test
    fun prolongedMissingUpdatesBecomeNoSignal() {
        val first = Instant.parse("2026-09-01T10:00:00Z")
        val pipeline = WalkingLocationPipeline(route)
        pipeline.accept(RawGpsPosition(41.005, -8.0, 5.0, first))

        val state = pipeline.markNoSignal(first.plusSeconds(31))

        assertEquals(GpsState.NO_SIGNAL, state.state)
        assertTrue(state.lastReliableObservation != null)
    }
}
