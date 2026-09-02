package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Focused tests for the single read model consumed by the walking UI. */
class WalkingStateCoordinatorTest {
    @Test
    fun `coordinator keeps one state when GPS moves and when signal is lost`() {
        val route = fixtureRoute()
        val walk = Walk(
            id = "sr-walk",
            routeId = route.id,
            plannedStartKm = 0.0,
            plannedDestinationKm = 1.0,
            actualStartKm = 0.0
        )
        val coordinator = WalkingStateCoordinator(route, walk, listOf(fixtureApoi()))

        val first = coordinator.accept(gps(40.00225, "2026-09-01T10:00:00Z"))
        assertEquals(GpsState.ON_ROUTE, first.gpsState)
        assertEquals("stage-1", first.progress?.stageId)
        assertEquals("sr-water", first.nextApoi?.id)
        assertTrue(first.nextApoiDistanceKm!! > 0.2)

        val beforeLossKm = first.routePosition!!.routeKm
        val lost = coordinator.markNoSignal(Instant.parse("2026-09-01T10:31:00Z"))
        assertEquals(GpsState.NO_SIGNAL, lost.gpsState)
        assertEquals(beforeLossKm, lost.routePosition!!.routeKm, 0.0001)
        assertEquals(first.nextApoi?.id, lost.nextApoi?.id)
    }

    @Test
    fun `offline flag changes presentation state without changing route position`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())

        val online = coordinator.accept(gps(40.00225, "2026-09-01T11:00:00Z"))
        val offline = coordinator.setOffline(true)

        assertTrue(offline.isOffline)
        assertEquals(online.routePosition!!.routeKm, offline.routePosition!!.routeKm, 0.0001)
        assertEquals(online.gpsState, offline.gpsState)
    }

    private fun gps(latitude: Double, capturedAt: String) = RawGpsPosition(
        latitude = latitude,
        longitude = -8.0,
        accuracyMeters = 5.0,
        capturedAt = Instant.parse(capturedAt)
    )

    private fun fixtureRoute() = Route(
        id = "sr-route",
        name = "Percurso SR",
        officialName = "SR — percurso sintético",
        totalDistanceKm = 1.0,
        source = "SR",
        updatedAt = "2026-09-01",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.0045, -8.0),
                GeoPoint(40.009, -8.0)
            )
        ),
        stages = listOf(
            Stage("stage-1", "sr-route", 1, "SR 1", 0.0, 0.5, 0.5, "Início", "Marco", "SR"),
            Stage("stage-2", "sr-route", 2, "SR 2", 0.5, 1.0, 0.5, "Marco", "Fim", "SR")
        )
    )

    private fun fixtureApoi() = Apoi(
        id = "sr-water",
        name = "Água SR",
        description = "APOI fictício de teste",
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            latitude = 40.0063,
            longitude = -8.0,
            precision = LocationPrecision.EXACT,
            locality = "SR",
            municipality = "SR",
            reference = "Fixture",
            routeId = "sr-route",
            routeKm = 0.7,
            distanceToRouteM = 0.0,
            accessDistanceM = 0.0,
            routeRelation = com.caminhos2027.v1.core.model.RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, "SR test data")
    )
}
