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
        val coordinator = WalkingStateCoordinator(
            route = route,
            initialWalk = Walk(
                id = "walk-1",
                routeId = route.id,
                plannedStartKm = 0.0,
                plannedDestinationKm = 1.0
            ),
            publishedApoi = listOf(water)
        )

        val started = coordinator.start(
            startPosition = RoutePosition(route.id, 0.10, 0.0, "stage-1"),
            now = Instant.parse("2026-09-03T10:00:00Z")
        )
        assertEquals("walk-1", started.walk.id)
        assertEquals(0.10, started.routePosition?.routeKm ?: -1.0, 0.0001)

        val moving = coordinator.accept(
            RawGpsPosition(
                latitude = 40.0040,
                longitude = -8.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-03T10:01:00Z")
            )
        )
        val startedKm = started.routePosition!!.routeKm
        assertTrue(moving.routePosition!!.routeKm > startedKm)
        assertEquals("water-1", moving.nextApoi?.id)

        val store = AppStateStore()
        store.setWalking(moving)
        val browser = ApoiBrowser(
            PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(water) }))
        )
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

    private fun fixtureRoute() = Route(
        id = "route-1",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 1.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-03",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.0045, -8.0),
                GeoPoint(40.009, -8.0)
            )
        ),
        stages = listOf(
            Stage("stage-1", "route-1", 1, "TEST/FICTITIOUS", 0.0, 1.0, 1.0, "Início", "Fim", "TEST/FICTITIOUS")
        )
    )

    private fun fixtureApoi() = Apoi(
        id = "water-1",
        name = "TEST/FICTITIOUS water",
        description = "APOI fictício de teste",
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            latitude = 40.0063,
            longitude = -8.0,
            precision = LocationPrecision.EXACT,
            locality = "TEST",
            municipality = "TEST",
            reference = "Fixture",
            routeId = "route-1",
            routeKm = 0.7,
            distanceToRouteM = 0.0,
            accessDistanceM = 0.0,
            routeRelation = RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, "TEST fixture")
    )
}
