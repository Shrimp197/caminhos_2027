package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppStateStore
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
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingApoiDecisionControllerTest {
    @Test
    fun browsingAndDecisionUseTheSameSharedWalkingState() {
        val route = route()
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        val walking = WalkingState(
            walk = WalkingPlanFactory.create(route, "walk", 0.0, 8.0),
            routePosition = RoutePosition("route", 4.0, 0.0, null),
            gpsState = GpsState.ON_ROUTE,
            progress = null,
            nextApoi = null,
            nextApoiDistanceKm = null
        )
        store.setWalking(walking)
        val controller = WalkingApoiDecisionController(route, catalog(water), store)

        val browsed = controller.browseApoi()
        val decided = controller.buildDecision()

        assertEquals(4.0, browsed.apoiBrowser?.query?.currentRouteKm ?: -1.0, 0.001)
        assertEquals(listOf("water"), browsed.apoiBrowser?.results?.map { it.apoi.id })
        assertEquals(4.0, decided.decision?.currentRouteKm ?: -1.0, 0.001)
        assertEquals(4.0, decided.decision?.remainingToPlannedDestinationKm ?: -1.0, 0.001)
    }

    @Test
    fun selectionDoesNotAlterWalkingState() {
        val route = route()
        val water = apoi("water", "Fonte", 5.0, ApoiCategory.AGUA)
        val store = AppStateStore()
        store.setWalking(WalkingState(
            walk = WalkingPlanFactory.create(route, "walk", 0.0, 8.0),
            routePosition = RoutePosition("route", 4.0, 0.0, null),
            gpsState = GpsState.ON_ROUTE,
            progress = null,
            nextApoi = null,
            nextApoiDistanceKm = null
        ))
        val controller = WalkingApoiDecisionController(route, catalog(water), store)

        controller.browseApoi()
        controller.selectApoi("water")

        assertEquals(4.0, store.state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals("water", store.state.apoiBrowser?.selected?.id)

        controller.clearApoiSelection()
        assertNull(store.state.apoiBrowser?.selected)
    }

    private fun route() = Route(
        id = "route",
        name = "Route",
        officialName = "Route",
        totalDistanceKm = 10.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0))),
        stages = emptyList()
    )

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
            null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
