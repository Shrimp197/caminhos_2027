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
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

class WalkingPreparationStartIntegrationTest {
    @Test
    fun savedPreparationTransitionsToActiveWalkingAndPublishesFirstPosition() {
        val route = route()
        val water = apoi("water", 3.0)
        val catalog = catalog(water)
        val store = AppStateStore()
        val service = WalkingPreparationService(route, InMemoryWalkRepository(), catalog)
        val preparation = WalkingPreparationAppStateController(route, service, store)

        preparation.save("walk", 1.0, 5.0)
        val state = preparation.startSaved(
            catalog = catalog,
            position = RoutePosition("route", 2.0, 0.0),
            now = Instant.parse("2026-09-03T10:00:00Z")
        )

        assertEquals(WalkStatus.ACTIVE, state.walking?.walk?.status)
        assertEquals(2.0, state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(GpsState.ACQUIRING, state.walking?.gpsState)
        assertEquals("water", state.walking?.nextApoi?.id)
    }

    @Test
    fun persistentRuntimeCarriesPreparationThroughGpsLossRecoveryAndStop() {
        val route = route()
        val catalog = catalog(apoi("water", 3.0))
        val walkRepository = InMemoryWalkRepository()
        val checkpointRepository = InMemoryWalkingStateRepository()
        val runtime = WalkingSessionRuntime(
            route = route,
            sessionService = WalkingSessionService(walkRepository, checkpointRepository),
            publishedApoi = catalog.all()
        )
        val store = AppStateStore()
        val service = WalkingPreparationService(route, walkRepository, catalog)
        val preparation = WalkingPreparationAppStateController(route, service, store, runtime)

        preparation.save("walk", 1.0, 5.0)
        val started = preparation.startSaved(
            catalog = catalog,
            position = RoutePosition("route", 2.0, 0.0),
            now = Instant.parse("2026-09-03T10:00:00Z")
        )
        assertEquals(WalkStatus.ACTIVE, started.walking?.walk?.status)
        assertEquals(2.0, started.walking?.routePosition?.routeKm ?: -1.0, 0.001)

        val controller = WalkingAppStateController(
            route = route,
            walk = requireNotNull(started.walking).walk,
            catalog = catalog,
            store = store,
            sessionRuntime = runtime
        )
        val moved = controller.acceptGps(
            RawGpsPosition(
                latitude = 40.03,
                longitude = -8.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-03T10:11:00Z")
            )
        )
        assertNotNull(moved.walking?.routePosition)
        val acceptedKm = moved.walking!!.routePosition!!.routeKm
        assertEquals(GpsState.ON_ROUTE, moved.walking.gpsState)

        val lost = controller.markNoSignal(Instant.parse("2026-09-03T10:12:00Z"))
        assertEquals(GpsState.NO_SIGNAL, lost.walking?.gpsState)
        assertEquals(acceptedKm, lost.walking?.routePosition?.routeKm ?: -1.0, 0.0001)

        val resumedRuntime = WalkingSessionRuntime(
            route = route,
            sessionService = WalkingSessionService(walkRepository, checkpointRepository),
            publishedApoi = catalog.all()
        )
        val resumedController = WalkingAppStateController(
            route = route,
            walk = requireNotNull(walkRepository.getActive()),
            catalog = catalog,
            store = AppStateStore(),
            sessionRuntime = resumedRuntime
        )
        val resumed = resumedController.resume(Instant.parse("2026-09-03T10:13:00Z"))
        assertEquals(GpsState.NO_SIGNAL, resumed.walking?.gpsState)
        assertEquals(acceptedKm, resumed.walking?.routePosition?.routeKm ?: -1.0, 0.0001)

        val recovered = resumedController.acceptGps(
            RawGpsPosition(
                latitude = 40.04,
                longitude = -8.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-03T10:24:00Z")
            )
        )
        assertEquals(GpsState.ON_ROUTE, recovered.walking?.gpsState)
        assertEquals(true, recovered.walking!!.routePosition!!.routeKm > acceptedKm)

        val stopped = resumedRuntime.stop(
            requireNotNull(recovered.walking).routePosition!!,
            Instant.parse("2026-09-03T10:25:00Z")
        )
        assertEquals(WalkStatus.COMPLETED, stopped.status)
        assertEquals(null, checkpointRepository.get("walk"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun startSavedRejectsPositionFromAnotherRoute() {
        val route = route()
        val catalog = catalog(apoi("water", 3.0))
        val store = AppStateStore()
        val service = WalkingPreparationService(route, InMemoryWalkRepository(), catalog)
        val preparation = WalkingPreparationAppStateController(route, service, store)
        preparation.save("walk", 1.0, 5.0)

        preparation.startSaved(
            catalog = catalog,
            position = RoutePosition("other-route", 2.0, 0.0),
            now = Instant.parse("2026-09-03T10:00:00Z")
        )
    }

    private fun catalog(vararg records: Apoi) =
        PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun route() = Route(
        id = "route",
        name = "Route",
        officialName = "Route",
        totalDistanceKm = 10.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.09, -8.0))),
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
