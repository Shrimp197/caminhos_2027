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
import com.caminhos2027.v1.core.model.Objective
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WalkingPreparationAppStateControllerTest {
    @Test
    fun savePublishesPlannedWalkWithoutStartingGps() {
        val route = route()
        val water = apoi("water", 3.0)
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val controller = WalkingPreparationAppStateController(route, service(route, repository, water), store)

        val state = controller.save("walk", 1.0, 5.0)

        assertEquals(WalkStatus.PLANNED, state.walking?.walk?.status)
        assertEquals(1.0, state.walking?.walk?.plannedStartKm ?: -1.0, 0.001)
        assertEquals(5.0, state.walking?.walk?.plannedDestinationKm ?: -1.0, 0.001)
        assertEquals(GpsState.NO_SIGNAL, state.walking?.gpsState)
        assertNull(state.walking?.routePosition)
        assertNull(state.walking?.nextApoi)
        assertEquals("walk", repository.getById("walk")?.id)
    }

    @Test
    fun savePersistsTheSamePreparedWalkThatWasPublished() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val controller = WalkingPreparationAppStateController(route, service(route, repository), store)

        controller.save("walk", 2.0, 7.0)

        assertEquals(store.state.walking?.walk, repository.getById("walk"))
    }

    @Test
    fun previewDoesNotPersistOrPublish() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val controller = WalkingPreparationAppStateController(route, service(route, repository), store)
        val before = store.state

        val preview = controller.preview("walk-preview", 2.0, 7.0)

        assertEquals(before, store.state)
        assertEquals(emptyList<Walk>(), repository.list())
        assertEquals("walk-preview", preview.walk.id)
        assertEquals(2.0, preview.walk.plannedStartKm ?: -1.0, 0.001)
        assertEquals(7.0, preview.walk.plannedDestinationKm ?: -1.0, 0.001)
    }

    @Test
    fun previewIncludesOnlyPublishedApoiInsidePreparationWindow() {
        val route = route()
        val inside = apoi("inside", 5.0)
        val before = apoi("before", 1.0)
        val after = apoi("after", 9.0)
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val controller = WalkingPreparationAppStateController(route, service(route, repository, inside, before, after), store)

        val preview = controller.preview("walk", 2.0, 8.0)

        assertEquals(listOf("inside"), preview.relevantApoi.map { it.id })
    }

    @Test
    fun previewExcludesUnpublishedApoiEvenWhenInsideWindow() {
        val route = route()
        val pending = apoi("pending", 5.0).copy(publication = ApoiPublication(PublicationStatus.REVIEW, null))
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val controller = WalkingPreparationAppStateController(route, service(route, repository, pending), store)

        val preview = controller.preview("walk", 2.0, 8.0)

        assertEquals(emptyList<Apoi>(), preview.relevantApoi)
    }

    @Test
    fun invalidSaveDoesNotPublishPartialWalkingState() {
        val route = route()
        val store = AppStateStore()
        val repository = InMemoryWalkRepository()
        val controller = WalkingPreparationAppStateController(route, service(route, repository), store)

        try {
            controller.save("walk-invalid", 8.0, 7.0)
        } catch (_: IllegalArgumentException) {
            // Expected: preparation validation must precede AppState publication.
        }

        assertNull(store.state.walking)
        assertNull(repository.getById("walk-invalid"))
    }

    @Test
    fun blankWalkIdDoesNotPublishPartialWalkingState() {
        val route = route()
        val store = AppStateStore()
        val repository = InMemoryWalkRepository()
        val controller = WalkingPreparationAppStateController(route, service(route, repository), store)

        try {
            controller.save("   ", 1.0, 5.0)
        } catch (_: IllegalArgumentException) {
            // Expected: invalid preparation must not reach shared state.
        }

        assertNull(store.state.walking)
        assertEquals(emptyList<Walk>(), repository.list())
    }

    @Test
    fun startSavedTransitionsThePreparedWalkToActiveAtTheSuppliedPosition() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val catalog = catalog(apoi("water", 5.0))
        val controller = WalkingPreparationAppStateController(route, WalkingPreparationService(route, repository, catalog), store)
        controller.save("walk", 2.0, 8.0)

        val now = Instant.parse("2026-09-04T10:00:00Z")
        controller.startSaved(RoutePosition("route", 2.5, 0.0, null, PositionConfidence.HIGH), now)

        val walking = requireNotNull(store.state.walking)
        assertEquals("walk", walking.walk.id)
        assertEquals(WalkStatus.ACTIVE, walking.walk.status)
        assertEquals(2.5, walking.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals("route", walking.routePosition?.routeId)
        assertNotNull(walking.walk.startedAt)
        assertEquals(now, walking.walk.startedAt)
    }

    @Test
    fun startSavedUsesOnlyTheWalkAlreadyStoredInSharedState() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val catalog = catalog()
        val controller = WalkingPreparationAppStateController(route, WalkingPreparationService(route, repository, catalog), store)
        controller.save("prepared", 2.0, 8.0)
        repository.save(WalkingPlanFactory.create(route, "different", 1.0, 6.0))

        controller.startSaved(RoutePosition("route", 2.5, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T10:00:00Z"))

        assertEquals("prepared", store.state.walking?.walk?.id)
        assertEquals(WalkStatus.ACTIVE, store.state.walking?.walk?.status)
    }

    @Test
    fun startSavedRejectsForeignRouteWithoutReplacingPreparedState() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val catalog = catalog()
        val controller = WalkingPreparationAppStateController(route, service(route, repository), store)
        controller.save("walk", 2.0, 8.0)

        try {
            controller.startSaved(RoutePosition("other-route", 2.5, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T10:00:00Z"))
            throw AssertionError("Expected foreign route rejection")
        } catch (_: IllegalArgumentException) {
            // Expected boundary rejection.
        }

        assertEquals(WalkStatus.PLANNED, store.state.walking?.walk?.status)
        assertNull(store.state.walking?.routePosition)
    }

    @Test
    fun startSavedRejectsNonFinitePositionWithoutReplacingPreparedState() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val catalog = catalog()
        val controller = WalkingPreparationAppStateController(route, service(route, repository), store)
        controller.save("walk", 2.0, 8.0)

        try {
            controller.startSaved(RoutePosition("route", Double.NaN, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T10:00:00Z"))
            throw AssertionError("Expected non-finite position rejection")
        } catch (_: IllegalArgumentException) {
            // Expected boundary rejection.
        }

        assertEquals(WalkStatus.PLANNED, store.state.walking?.walk?.status)
        assertNull(store.state.walking?.routePosition)
    }

    @Test
    fun startSavedPreservesExistingNonWalkingSlices() {
        val route = route()
        val repository = InMemoryWalkRepository()
        val store = AppStateStore()
        val catalog = catalog(apoi("water", 5.0))
        val controller = WalkingPreparationAppStateController(route, WalkingPreparationService(route, repository, catalog), store)
        val browser = com.caminhos2027.v1.core.apoi.ApoiBrowser(catalog)
        val objective = Objective("objective", "route", "Fátima", targetRouteKm = 10.0)
        store.setObjective(objective)
        store.setDataVersion("2027-test")
        controller.save("walk", 2.0, 8.0)
        store.setWalking(requireNotNull(store.state.walking).copy(routePosition = RoutePosition("route", 2.0, 0.0, null, PositionConfidence.HIGH)))
        store.browseApoi(browser)
        val beforeBrowser = store.state.apoiBrowser

        controller.startSaved(RoutePosition("route", 2.5, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T10:00:00Z"))

        assertEquals(beforeBrowser, store.state.apoiBrowser)
        assertEquals(objective, store.state.objective)
        assertEquals("2027-test", store.state.dataVersion)
    }

    @Test(expected = IllegalArgumentException::class)
    fun startSavedRequiresPreparedState() {
        val route = route()
        val store = AppStateStore()
        val catalog = catalog()
        val controller = WalkingPreparationAppStateController(route, service(route, InMemoryWalkRepository()), store)

        controller.startSaved(RoutePosition("route", 2.5, 0.0, null, PositionConfidence.HIGH), Instant.parse("2026-09-04T10:00:00Z"))
    }

    private fun service(route: Route, repository: WalkRepository, vararg records: Apoi) = WalkingPreparationService(route, repository, catalog(*records))

    private fun catalog(vararg records: Apoi) = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun route() = Route(
        id = "route", name = "Route", officialName = "Route", totalDistanceKm = 10.0,
        source = "test", updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0, -7.8828))), stages = emptyList()
    )

    private fun apoi(id: String, km: Double) = Apoi(
        id = id, name = "Fonte", description = null, mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
