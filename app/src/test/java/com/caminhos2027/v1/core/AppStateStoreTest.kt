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
import com.caminhos2027.v1.core.model.Objective
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingPlanFactory
import com.caminhos2027.v1.core.walking.WalkingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppStateStoreTest {
    @Test fun browsingUsesTheCurrentWalkingPosition() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser, filter = ApoiFilter(services = setOf(ApoiCategory.AGUA)))
        assertEquals(4.0, store.state.apoiBrowser?.query?.currentRouteKm ?: -1.0, 0.001)
        assertEquals(listOf("water"), store.state.apoiBrowser?.results?.map { it.apoi.id })
    }

    @Test fun selectionAndClearingStayInsideSharedBrowserState() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser)
        store.selectApoi(browser, "water")
        assertEquals("water", store.state.apoiBrowser?.selected?.id)
        store.clearApoiSelection(browser)
        assertNull(store.state.apoiBrowser?.selected)
        assertEquals(listOf("water"), store.state.apoiBrowser?.results?.map { it.apoi.id })
    }

    @Test fun clearingBrowserIsSafeWhenNoBrowserWasOpened() {
        val store = AppStateStore()
        store.setWalking(walkingState(4.0))
        store.clearApoiBrowser()
        assertNull(store.state.apoiBrowser)
        assertEquals(4.0, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test fun decisionUsesTheSameCurrentWalkingPosition() {
        val route = route()
        val store = AppStateStore()
        store.setWalking(walkingState(4.0, plannedDestinationKm = 8.0))
        store.buildDecision(route, emptyList())
        assertEquals(4.0, store.state.decision?.currentRouteKm ?: -1.0, 0.001)
        assertEquals(4.0, store.state.decision?.remainingToPlannedDestinationKm ?: -1.0, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun browsingRequiresAUsableWalkingPosition() {
        val store = AppStateStore()
        store.browseApoi(ApoiBrowser(catalog()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun decisionRejectsWalkingPositionFromAnotherRoute() {
        val store = AppStateStore()
        store.setWalking(walkingState(4.0).copy(routePosition = RoutePosition("other-route", 4.0, 0.0, null, PositionConfidence.HIGH)))
        store.buildDecision(route(), emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun decisionRejectsNonFiniteWalkingPosition() {
        val store = AppStateStore()
        store.setWalking(walkingState(4.0).copy(routePosition = RoutePosition("route", Double.NaN, 0.0, null, PositionConfidence.HIGH)))
        store.buildDecision(route(), emptyList())
    }

    @Test fun clearingBrowserDoesNotClearWalkingOrDecision() {
        val route = route()
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser)
        store.buildDecision(route, listOf(water))
        store.clearApoiBrowser()
        assertNull(store.state.apoiBrowser)
        assertEquals(4.0, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(4.0, store.state.decision?.currentRouteKm ?: -1.0, 0.001)
    }

    @Test fun clearingDecisionDoesNotClearWalkingOrBrowser() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser)
        store.buildDecision(route(), listOf(water))
        store.clearDecision()
        assertNull(store.state.decision)
        assertEquals(4.0, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(listOf("water"), store.state.apoiBrowser?.results?.map { it.apoi.id })
    }

    @Test fun replacingWalkingDoesNotEraseExistingBrowserOrDecisionSlices() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser)
        store.buildDecision(route(), listOf(water))
        store.setWalking(walkingState(4.5))
        assertEquals(4.5, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(4.0, store.state.apoiBrowser?.query?.currentRouteKm ?: -1.0, 0.001)
        assertEquals(4.0, store.state.decision?.currentRouteKm ?: -1.0, 0.001)
    }

    @Test fun rejectedDecisionLeavesPreviouslyBuiltDecisionUntouched() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val route = route()
        val store = AppStateStore()
        store.setWalking(walkingState(4.0))
        store.buildDecision(route, listOf(water))
        val before = store.state.decision
        store.setWalking(walkingState(4.0).copy(routePosition = RoutePosition("other-route", 4.0, 0.0, null, PositionConfidence.HIGH)))
        try {
            store.buildDecision(route, listOf(water))
            throw AssertionError("Expected decision build to reject a foreign route")
        } catch (_: IllegalArgumentException) {
            // Expected boundary rejection; the previous decision must remain intact.
        }
        assertEquals(before, store.state.decision)
    }

    @Test fun failedApoiSelectionDoesNotEraseExistingBrowserResults() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser)
        store.selectApoi(browser, "missing")
        assertEquals(listOf("water"), store.state.apoiBrowser?.results?.map { it.apoi.id })
        assertNull(store.state.apoiBrowser?.selected)
    }

    @Test fun objectiveAndDataVersionChangesPreserveOtherSlices() {
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val browser = ApoiBrowser(catalog(water))
        store.setWalking(walkingState(4.0))
        store.browseApoi(browser)
        val beforeBrowser = store.state.apoiBrowser
        store.setObjective(Objective.REACH_DESTINATION)
        store.setDataVersion("2027-test")
        assertEquals(Objective.REACH_DESTINATION, store.state.objective)
        assertEquals("2027-test", store.state.dataVersion)
        assertEquals(beforeBrowser, store.state.apoiBrowser)
        assertEquals(4.0, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test fun clearingDecisionIsIdempotentAndPreservesObjectiveAndVersion() {
        val store = AppStateStore()
        store.setWalking(walkingState(4.0))
        store.setObjective(Objective.REACH_DESTINATION)
        store.setDataVersion("2027-test")
        store.clearDecision()
        store.clearDecision()
        assertNull(store.state.decision)
        assertEquals(Objective.REACH_DESTINATION, store.state.objective)
        assertEquals("2027-test", store.state.dataVersion)
        assertEquals(4.0, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test fun clearingApoiBrowserIsIdempotentAndPreservesDecision() {
        val route = route()
        val store = AppStateStore()
        store.setWalking(walkingState(4.0))
        store.buildDecision(route, emptyList())
        store.clearApoiBrowser()
        store.clearApoiBrowser()
        assertNull(store.state.apoiBrowser)
        assertEquals(4.0, store.state.decision?.currentRouteKm ?: -1.0, 0.001)
    }

    private fun route() = Route(
        id = "route", name = "Route", officialName = "Route", totalDistanceKm = 10.0, source = "test", updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.01, -8.0))), stages = emptyList()
    )

    private fun catalog(vararg records: Apoi) = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun apoi(id: String, name: String, km: Double, category: ApoiCategory) = Apoi(
        id, name, null, category, setOf(category),
        ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE),
        ApoiPublication(PublicationStatus.PUBLISHED, null)
    )

    private fun walkingState(routeKm: Double, plannedDestinationKm: Double = 10.0) = WalkingState(
        walk = WalkingPlanFactory.create(route(), "walk", 0.0, plannedDestinationKm).copy(status = WalkStatus.ACTIVE, actualStartKm = 0.0),
        routePosition = RoutePosition("route", routeKm, 0.0, null, PositionConfidence.HIGH),
        gpsState = GpsState.ON_ROUTE, progress = null, nextApoi = null, nextApoiDistanceKm = null, isOffline = false
    )
}
