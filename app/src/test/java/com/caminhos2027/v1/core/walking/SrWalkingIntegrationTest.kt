package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SR is synthetic integration data and must never be promoted to production. */
class SrWalkingIntegrationTest {
    @Test
    fun `gps movement updates position stage progress and next APOI`() {
        val route = SrFixture.route()
        val walk = Walk("sr-walk-1", route.id, plannedStartKm = 0.0, plannedDestinationKm = 1.0, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, SrFixture.publishedApoi())

        val first = coordinator.accept(SrFixture.gps(40.00225, "2026-09-01T08:10:00Z"))
        assertEquals(GpsState.ON_ROUTE, first.gpsState)
        assertEquals("stage-1", first.routePosition?.stageId)
        assertTrue(first.progress!!.walkedKm > 0.2)
        assertEquals("sr-water-1", first.nextApoi?.id)
        assertTrue(first.nextApoiDistanceKm!! > 0.2)

        val second = coordinator.accept(SrFixture.gps(40.00495, "2026-09-01T08:30:00Z"))
        assertEquals(GpsState.ON_ROUTE, second.gpsState)
        assertEquals("stage-2", second.routePosition?.stageId)
        assertTrue(second.progress!!.walkedKm > first.progress.walkedKm)
        assertTrue(second.nextApoiDistanceKm!! < first.nextApoiDistanceKm!!)
    }

    @Test
    fun `offline and GPS loss preserve the last reliable position and recovery refreshes derived state`() {
        val route = SrFixture.route()
        val walk = Walk("sr-walk-2", route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, SrFixture.publishedApoi())

        val online = coordinator.accept(SrFixture.gps(40.00225, "2026-09-01T09:00:00Z"))
        val routeKm = online.routePosition!!.routeKm
        val walkedKm = online.progress!!.walkedKm
        val nextApoiDistanceKm = online.nextApoiDistanceKm!!

        val offline = coordinator.setOffline(true)
        assertTrue(offline.isOffline)
        assertEquals(routeKm, offline.routePosition!!.routeKm, 0.0001)
        assertEquals(walkedKm, offline.progress!!.walkedKm, 0.0001)
        assertEquals(nextApoiDistanceKm, offline.nextApoiDistanceKm!!, 0.0001)

        val noSignal = coordinator.markNoSignal(Instant.parse("2026-09-01T09:31:00Z"))
        assertEquals(GpsState.NO_SIGNAL, noSignal.gpsState)
        assertEquals(routeKm, noSignal.routePosition!!.routeKm, 0.0001)
        assertEquals(walkedKm, noSignal.progress!!.walkedKm, 0.0001)
        assertNotNull(noSignal.nextApoi)

        val recovered = coordinator.accept(SrFixture.gps(40.00630, "2026-09-01T09:35:00Z"))
        assertEquals(GpsState.ON_ROUTE, recovered.gpsState)
        assertTrue(recovered.routePosition!!.routeKm > routeKm)
        assertEquals("stage-2", recovered.routePosition.stageId)
        assertTrue(recovered.progress!!.walkedKm > walkedKm)
        assertEquals("sr-water-1", recovered.nextApoi?.id)
        assertTrue(recovered.nextApoiDistanceKm!! < nextApoiDistanceKm)
    }
}

private object SrFixture {
    fun route() = Route(
        id = "sr-route-001",
        name = "Percurso de teste SR",
        officialName = "SR — percurso sintético de integração",
        totalDistanceKm = 1.0,
        source = "SR synthetic fixture",
        updatedAt = "2026-09-01",
        geometry = RouteGeometry(listOf(
            GeoPoint(40.00000, -8.00000),
            GeoPoint(40.00450, -8.00000),
            GeoPoint(40.00900, -8.00000)
        )),
        stages = listOf(
            Stage("stage-1", "sr-route-001", 1, "SR — etapa 1", 0.0, 0.5, 0.5, "Início SR", "Marco SR 1", "SR"),
            Stage("stage-2", "sr-route-001", 2, "SR — etapa 2", 0.5, 1.0, 0.5, "Marco SR 1", "Fim SR", "SR")
        )
    )

    fun publishedApoi() = listOf(Apoi(
        id = "sr-water-1",
        name = "Água SR — teste",
        description = "APOI fictício de teste.",
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.00630, -8.00000, LocationPrecision.EXACT, "SR", "SR", "Fixture", "sr-route-001", 0.70, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, "SR test data")
    ))

    fun gps(latitude: Double, at: String) = RawGpsPosition(latitude, -8.00000, 5.0, Instant.parse(at))
}
