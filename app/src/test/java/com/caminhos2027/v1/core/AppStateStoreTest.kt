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
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgress
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

    @Test fun decisionUsesTheSameCurrentWalkingPosition() {
        val route = Route("route", "Route", 10.0, emptyList())
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

    private fun catalog(vararg records: Apoi) =
        PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun apoi(id: String, name: String, km: Double, category: ApoiCategory) = Apoi(
        id = id,
        name = name,
        description = null,
        mainCategory = category,
        services = setOf(category),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", km, 0.0, 0.0, com.caminhos2027.v1.core.model.RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )

    private fun walkingState(routeKm: Double, plannedDestinationKm: Double = 10.0) = WalkingState(
        walk = Walk("walk", "route", emptyList(), null, plannedDestinationKm, null, null, WalkStatus.ACTIVE),
        routePosition = RoutePosition("route", routeKm, 0.0, null, com.caminhos2027.v1.core.model.PositionConfidence.HIGH),
        gpsState = GpsState.ON_ROUTE,
        progress = WalkingProgress(routeKm, 10.0 - routeKm, routeKm / 10.0),
        nextApoi = null,
        nextApoiDistanceKm = null,
        isOffline = false
    )
}
