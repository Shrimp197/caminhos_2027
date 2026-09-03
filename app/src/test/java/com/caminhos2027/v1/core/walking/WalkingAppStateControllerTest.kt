package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class WalkingAppStateControllerTest {
    @Test
    fun startingWalkingMakesTheWalkActiveAndPublishesPosition() {
        val controller = controller()

        val state = controller.start(RoutePosition("route", 2.0, 0.0), java.time.Instant.parse("2026-09-02T10:00:00Z"))

        assertEquals(WalkStatus.ACTIVE, state.walking?.walk?.status)
        assertEquals(2.0, state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(com.caminhos2027.v1.core.route.GpsState.ACQUIRING, state.walking?.gpsState)
    }

    @Test
    fun controllerPublishesIntoTheProvidedStore() {
        val store = AppStateStore()
        val controller = controller(store)

        val state = controller.start(RoutePosition("route", 2.0, 0.0))

        assertEquals(state, store.state)
    }

    @Test
    fun offlineChangeIsPublishedWithoutChangingPosition() {
        val controller = controller()
        controller.start(RoutePosition("route", 2.0, 0.0))

        val state = controller.setOffline(true)

        assertEquals(2.0, state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(true, state.walking?.isOffline)
    }

    private fun controller(store: AppStateStore = AppStateStore()) =
        WalkingAppStateController(
            route = route(),
            walk = WalkingPlanFactory.create(route(), "walk", 0.0, 10.0),
            catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() })),
            store = store
        )

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
}
