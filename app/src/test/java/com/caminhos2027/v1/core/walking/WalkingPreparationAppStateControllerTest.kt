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
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingPreparationAppStateControllerTest {
    @Test
    fun savePublishesPlannedWalkWithoutStartingGps() {
        val route = route()
        val water = apoi("water", 3.0)
        val store = AppStateStore()
        val service = WalkingPreparationService(
            route = route,
            walkRepository = InMemoryWalkRepository(),
            apoiCatalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(water) }))
        )
        val controller = WalkingPreparationAppStateController(route, service, store)

        val state = controller.save("walk", 1.0, 5.0)

        assertEquals(WalkStatus.PLANNED, state.walking?.walk?.status)
        assertEquals(1.0, state.walking?.walk?.plannedStartKm ?: -1.0, 0.001)
        assertEquals(5.0, state.walking?.walk?.plannedDestinationKm ?: -1.0, 0.001)
        assertEquals(GpsState.NO_SIGNAL, state.walking?.gpsState)
        assertEquals(null, state.walking?.routePosition)
        assertEquals(null, state.walking?.nextApoi)
    }

    @Test
    fun invalidSaveDoesNotPublishPartialWalkingState() {
        val route = route()
        val store = AppStateStore()
        val service = WalkingPreparationService(
            route = route,
            walkRepository = InMemoryWalkRepository(),
            apoiCatalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() }))
        )
        val controller = WalkingPreparationAppStateController(route, service, store)

        try {
            controller.save("walk-invalid", 8.0, 7.0)
        } catch (_: IllegalArgumentException) {
            // Expected: preparation validation must precede AppState publication.
        }

        assertNull(store.state.walking)
    }

    @Test
    fun blankWalkIdDoesNotPublishPartialWalkingState() {
        val route = route()
        val store = AppStateStore()
        val service = WalkingPreparationService(
            route = route,
            walkRepository = InMemoryWalkRepository(),
            apoiCatalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() }))
        )
        val controller = WalkingPreparationAppStateController(route, service, store)

        try {
            controller.save("   ", 1.0, 5.0)
        } catch (_: IllegalArgumentException) {
            // Expected: invalid preparation must not reach shared state.
        }

        assertNull(store.state.walking)
    }

    private fun route() = Route(
        id = "route",
        name = "Route",
        officialName = "Route",
        totalDistanceKm = 10.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0, -7.8828))),
        stages = emptyList()
    )

    private fun apoi(id: String, km: Double) = Apoi(
        id = id,
        name = "Fonte",
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
