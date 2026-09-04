package com.caminhos2027.v1.core

import com.caminhos2027.v1.core.apoi.ApoiBrowser
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
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
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingStateCoordinator
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the V1 walking-to-APOI-to-decision seam without introducing a UI coordinator. */
class V1WalkingFlowIntegrationTest {
    @Test
    fun walkingContextSurvivesApoiAndDecisionFlow() {
        val route = fixtureRoute()
        val water = fixtureApoi()
        val coordinator = WalkingStateCoordinator(route, Walk(id = "walk-1", routeId = route.id, plannedStartKm = 0.0, plannedDestinationKm = 1.0), listOf(water))

        val started = coordinator.start(RoutePosition(route.id, 0.10, 0.0, "stage-1"), Instant.parse("2026-09-03T10:00:00Z"))
        assertEquals("walk-1", started.walk.id)
        assertEquals(0.10, started.routePosition?.routeKm ?: -1.0, 0.0001)

        val moving = coordinator.accept(RawGpsPosition(40.0020, -8.0, 5.0, Instant.parse("2026-09-03T10:01:00Z")))
        val startedKm = started.routePosition!!.routeKm
        assertTrue(moving.routePosition!!.routeKm > startedKm)
        assertEquals("water-1", moving.nextApoi?.id)

        val store = AppStateStore()
        store.setWalking(moving)
        val browser = ApoiBrowser(PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(water) })))
        store.browseApoi(browser, filter = ApoiFilter(services = setOf(ApoiCategory.AGUA)))
        assertEquals(1, store.state.apoiBrowser?.results?.size)
        assertEquals("water-1", store.state.apoiBrowser?.results?.single()?.apoi?.id)
        store.selectApoi(browser, "water-1")
        assertEquals("water-1", store.state.apoiBrowser?.selected?.id)
        assertSame(moving, store.state.walking)

        store.buildDecision(route, listOf(water))
        val decision = store.state.decision
        assertNotNull(decision)
        val currentRouteKm = moving.routePosition!!.routeKm
        val plannedDestinationKm = moving.walk.plannedDestinationKm!!
        assertEquals(currentRouteKm, decision!!.currentRouteKm, 0.0001)
        assertEquals(plannedDestinationKm - currentRouteKm, decision.remainingToPlannedDestinationKm, 0.0001)
        assertSame(moving, store.state.walking)

        store.clearApoiSelection(browser)
        assertEquals(null, store.state.apoiBrowser?.selected)
        assertSame(moving, store.state.walking)
        assertNotNull(store.state.decision)
    }

    @Test
    fun rejectedGpsCannotBreakWalkingApoiDecisionContext() {
        val route = fixtureRoute()
        val water = fixtureApoi()
        val coordinator = WalkingStateCoordinator(route, Walk(id = "walk-reject", routeId = route.id, plannedStartKm = 0.0, plannedDestinationKm = 1.0), listOf(water))
        coordinator.start(RoutePosition(route.id, 0.0, 0.0), Instant.parse("2026-09-03T11:00:00Z"))
        val reliable = coordinator.accept(RawGpsPosition(40.00225, -8.0, 5.0, Instant.parse("2026-09-03T11:02:00Z")))
        val beforeKm = reliable.routePosition!!.routeKm
        val beforeApoi = reliable.nextApoi
        val rejected = coordinator.accept(RawGpsPosition(40.009, -8.0, 5.0, Instant.parse("2026-09-03T11:02:01Z")))

        assertEquals(beforeKm, rejected.routePosition!!.routeKm, 0.0001)
        assertEquals(beforeApoi, rejected.nextApoi)
        assertEquals(GpsState.ON_ROUTE, reliable.gpsState)
        assertEquals(GpsState.ON_ROUTE, rejected.gpsState)
    }

    @Test
    fun signalLossPreservesDecisionContextUntilRecovery() {
        val route = fixtureRoute()
        val water = fixtureApoi()
        val coordinator = WalkingStateCoordinator(route, Walk(id = "walk-recovery", routeId = route.id, plannedStartKm = 0.0, plannedDestinationKm = 1.0), listOf(water))
        val store = AppStateStore()
        val started = coordinator.start(RoutePosition(route.id, 0.0, 0.0), Instant.parse("2026-09-03T12:00:00Z"))
        val moving = coordinator.accept(RawGpsPosition(40.00225, -8.0, 5.0, Instant.parse("2026-09-03T12:02:00Z")))
        store.setWalking(moving)
        store.buildDecision(route, listOf(water))
        val decisionBefore = store.state.decision

        val noSignal = coordinator.markNoSignal(Instant.parse("2026-09-03T12:02:31Z"))
        store.setWalking(noSignal)

        assertEquals(GpsState.NO_SIGNAL, noSignal.gpsState)
        assertEquals(moving.routePosition, noSignal.routePosition)
        assertEquals(moving.nextApoi, noSignal.nextApoi)
        assertEquals(moving.nextApoiDistanceKm, noSignal.nextApoiDistanceKm)
        assertSame(decisionBefore, store.state.decision)
        assertEquals(started.walk.id, noSignal.walk.id)

        val recovered = coordinator.accept(RawGpsPosition(40.0045, -8.0, 5.0, Instant.parse("2026-09-03T12:04:00Z")))
        store.setWalking(recovered)
        store.buildDecision(route, listOf(water))

        assertEquals(GpsState.ON_ROUTE, recovered.gpsState)
        assertTrue(recovered.routePosition!!.routeKm > moving.routePosition!!.routeKm)
        assertEquals("water-1", recovered.nextApoi?.id)
        assertTrue(store.state.decision!!.currentRouteKm > decisionBefore!!.currentRouteKm)
        assertSame(recovered, store.state.walking)
    }

    @Test
    fun offlinePresentationKeepsWalkingAndDecisionContextStable() {
        val route = fixtureRoute()
        val water = fixtureApoi()
        val coordinator = WalkingStateCoordinator(route, Walk(id = "walk-offline", routeId = route.id, plannedStartKm = 0.0, plannedDestinationKm = 1.0), listOf(water))
        val store = AppStateStore()
        val moving = coordinator.accept(
            RawGpsPosition(40.00225, -8.0, 5.0, Instant.parse("2026-09-03T13:02:00Z"))
        )
        store.setWalking(moving)
        store.buildDecision(route, listOf(water))
        val decisionBefore = store.state.decision

        val offline = moving.copy(isOffline = true)
        store.setWalking(offline)

        assertTrue(store.state.walking!!.isOffline)
        assertEquals(moving.routePosition, store.state.walking!!.routePosition)
        assertEquals(moving.nextApoi, store.state.walking!!.nextApoi)
        assertEquals(decisionBefore, store.state.decision)
        assertEquals(moving.walk.id, store.state.walking!!.walk.id)
    }

    private fun fixtureRoute() = Route(
        id = "route-1", name = "TEST/FICTITIOUS route", officialName = "TEST/FICTITIOUS route", totalDistanceKm = 1.0,
        source = "TEST/FICTITIOUS", updatedAt = "2026-09-03",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0045, -8.0), GeoPoint(40.009, -8.0))),
        stages = listOf(Stage("stage-1", "route-1", 1, "TEST/FICTITIOUS", 0.0, 1.0, 1.0, "Início", "Fim", "TEST/FICTITIOUS"))
    )

    private fun fixtureApoi() = Apoi(
        id = "water-1", name = "TEST/FICTITIOUS water", description = "APOI fictício de teste", mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0063, -8.0, LocationPrecision.EXACT, "TEST", "TEST", "Fixture", "route-1", 0.7, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, "TEST fixture")
    )
}
