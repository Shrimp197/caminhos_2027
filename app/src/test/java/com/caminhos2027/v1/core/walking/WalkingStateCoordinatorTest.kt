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
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingMovementCue
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `valid GPS movement updates route position and next APOI distance`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, listOf(fixtureApoi()))

        val first = coordinator.accept(gps(40.00225, "2026-09-01T11:00:00Z"))
        val moved = coordinator.accept(gps(40.00300, "2026-09-01T11:01:00Z"))

        assertTrue(moved.routePosition!!.routeKm > first.routePosition!!.routeKm)
        assertEquals("sr-water", moved.nextApoi?.id)
        assertTrue(moved.nextApoiDistanceKm!! < first.nextApoiDistanceKm!!)
    }

    @Test
    fun `coordinator exposes forward movement only after consecutive reliable positions`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())

        val first = coordinator.accept(gps(40.00225, "2026-09-01T11:10:00Z"))
        assertNull(first.movementCue)

        val moved = coordinator.accept(gps(40.00320, "2026-09-01T11:11:00Z"))
        assertEquals(WalkingMovementCue.FORWARD, moved.movementCue)
    }

    @Test
    fun `coordinator exposes backward movement when reliable route distance decreases`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())

        coordinator.accept(gps(40.00550, "2026-09-01T11:20:00Z"))
        val movedBack = coordinator.accept(gps(40.00450, "2026-09-01T11:21:00Z"))

        assertEquals(WalkingMovementCue.BACKWARD, movedBack.movementCue)
    }

    @Test
    fun `small reliable route changes remain stationary`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())

        coordinator.accept(gps(40.00300, "2026-09-01T11:30:00Z"))
        val nearlySame = coordinator.accept(gps(40.00308, "2026-09-01T11:31:00Z"))

        assertEquals(WalkingMovementCue.STATIONARY, nearlySame.movementCue)
    }

    @Test
    fun `identical reliable route positions remain stationary`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())

        val first = coordinator.accept(gps(40.00300, "2026-09-01T11:35:00Z"))
        val same = coordinator.accept(gps(40.00300, "2026-09-01T11:36:00Z"))

        assertNull(first.movementCue)
        assertEquals(WalkingMovementCue.STATIONARY, same.movementCue)
    }

    @Test
    fun `implausible GPS jump does not move walking position or next APOI`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, listOf(fixtureApoi()))

        val first = coordinator.accept(gps(40.00225, "2026-09-01T12:00:00Z"))
        val jump = coordinator.accept(gps(40.00900, "2026-09-01T12:00:10Z"))

        assertEquals(first.routePosition!!.routeKm, jump.routePosition!!.routeKm, 0.0001)
        assertEquals(first.nextApoi?.id, jump.nextApoi?.id)
        assertEquals(first.nextApoiDistanceKm!!, jump.nextApoiDistanceKm!!, 0.0001)
        assertNull(jump.movementCue)
    }

    @Test
    fun `start position outside possible-deviation threshold is provisional`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())
        val provisional = RoutePosition(
            routeId = route.id,
            routeKm = 0.3,
            distanceToRouteMeters = 40.0,
            stageId = "stage-1"
        )

        val started = coordinator.start(provisional, Instant.parse("2026-09-01T13:00:00Z"))

        assertEquals(GpsState.ACQUIRING, started.gpsState)
        assertEquals(provisional.routeKm, started.routePosition!!.routeKm, 0.0001)
        assertNull(coordinator.lastReliableObservedAt())
    }

    @Test
    fun `start position inside possible-deviation threshold can establish reliable baseline`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())
        val baseline = RoutePosition(
            routeId = route.id,
            routeKm = 0.3,
            distanceToRouteMeters = 34.0,
            stageId = "stage-1"
        )
        val now = Instant.parse("2026-09-01T13:00:00Z")

        coordinator.start(baseline, now)

        assertEquals(now, coordinator.lastReliableObservedAt())
    }

    @Test
    fun `checkpoint with malformed position is discarded safely`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())
        val malformed = WalkingCheckpoint(
            routePosition = RoutePosition(route.id, Double.NaN, -1.0, "stage-1"),
            gpsState = GpsState.ON_ROUTE,
            isOffline = false,
            lastObservedAt = Instant.parse("2026-09-01T14:00:00Z")
        )

        val restored = coordinator.restoreCheckpoint(malformed, Instant.parse("2026-09-01T14:05:00Z"))

        assertEquals(GpsState.NO_SIGNAL, restored.gpsState)
        assertNull(restored.routePosition)
        assertNull(coordinator.lastReliableObservedAt())
    }

    @Test
    fun `checkpoint without observation timestamp restores position but waits for fresh GPS baseline`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())
        val checkpointPosition = RoutePosition(route.id, 0.3, 0.0, "stage-1")
        val checkpoint = WalkingCheckpoint(
            routePosition = checkpointPosition,
            gpsState = GpsState.NO_SIGNAL,
            isOffline = true,
            lastObservedAt = null
        )

        val restored = coordinator.restoreCheckpoint(checkpoint, Instant.parse("2026-09-01T14:05:00Z"))

        assertEquals(GpsState.NO_SIGNAL, restored.gpsState)
        assertEquals(checkpointPosition.routeKm, restored.routePosition!!.routeKm, 0.0001)
        assertNull(coordinator.lastReliableObservedAt())

        val gpsAt = Instant.parse("2026-09-01T14:06:00Z")
        val recovered = coordinator.accept(gps(40.00225, gpsAt.toString()))
        assertEquals(GpsState.ON_ROUTE, recovered.gpsState)
        assertTrue(recovered.routePosition!!.routeKm > 0.2)
        assertEquals(gpsAt, coordinator.lastReliableObservedAt())
    }

    @Test
    fun `checkpoint from another route is discarded safely`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val coordinator = WalkingStateCoordinator(route, walk, emptyList())
        val checkpoint = WalkingCheckpoint(
            routePosition = RoutePosition("another-route", 0.3, 0.0, "stage-1"),
            gpsState = GpsState.ON_ROUTE,
            isOffline = false,
            lastObservedAt = Instant.parse("2026-09-01T15:00:00Z")
        )

        val restored = coordinator.restoreCheckpoint(checkpoint, Instant.parse("2026-09-01T15:05:00Z"))

        assertEquals(GpsState.NO_SIGNAL, restored.gpsState)
        assertNull(restored.routePosition)
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
