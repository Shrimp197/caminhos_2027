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
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.WalkStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingAppStateControllerTest {
    @Test
    fun startingWalkingMakesTheWalkActiveAndPublishesPosition() {
        val controller = controller()

        val state = controller.start(RoutePosition("route", 2.0, 0.0), Instant.parse("2026-09-02T10:00:00Z"))

        assertEquals(WalkStatus.ACTIVE, state.walking?.walk?.status)
        assertEquals(2.0, state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(com.caminhos2027.v1.core.route.GpsState.ACQUIRING, state.walking?.gpsState)
    }

    @Test
    fun controllerPublishesIntoTheProvidedStore() {
        val store = AppStateStore()
        val controller = controller(store = store)

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

    @Test
    fun validGpsMovementRecomputesNextApoiFromTheSharedWalkingPosition() {
        val controller = controller(catalog(waterApoi()))
        controller.start(RoutePosition("route", 0.0, 0.0), Instant.parse("2026-09-02T11:00:00Z"))

        val initial = controller.acceptGps(gps(40.00225, "2026-09-02T11:02:00Z"))
        val initialDistance = initial.walking!!.nextApoiDistanceKm!!

        val moved = controller.acceptGps(gps(40.0045, "2026-09-02T11:04:00Z"))

        assertTrue(moved.walking!!.routePosition!!.routeKm > initial.walking!!.routePosition!!.routeKm)
        assertEquals("water", moved.walking!!.nextApoi?.id)
        assertTrue(moved.walking!!.nextApoiDistanceKm!! < initialDistance)
    }

    @Test
    fun implausibleGpsJumpDoesNotMoveWalkingPositionOrNextApoi() {
        val controller = controller(catalog(waterApoi()))
        controller.start(RoutePosition("route", 0.0, 0.0), Instant.parse("2026-09-02T12:00:00Z"))

        val reliable = controller.acceptGps(gps(40.00225, "2026-09-02T12:02:00Z"))
        val reliableKm = reliable.walking!!.routePosition!!.routeKm
        val reliableApoiDistance = reliable.walking!!.nextApoiDistanceKm

        val jumped = controller.acceptGps(gps(40.009, "2026-09-02T12:02:01Z"))

        assertEquals(reliableKm, jumped.walking!!.routePosition!!.routeKm, 0.0001)
        assertEquals(reliableApoiDistance, jumped.walking!!.nextApoiDistanceKm)
    }

    @Test
    fun persistentRuntimeKeepsLastReliableObservedAtAfterRejectedJump() {
        val route = route()
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val plan = WalkingPlanFactory.create(route, "walk-persist-time", 0.0, 1.0)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        val controller = WalkingAppStateController(
            route = route,
            walk = plan,
            catalog = catalog(),
            store = AppStateStore(),
            sessionRuntime = runtime
        )
        controller.start(RoutePosition("route", 0.0, 0.0), Instant.parse("2026-09-02T13:00:00Z"))
        controller.acceptGps(gps(40.00225, "2026-09-02T13:02:00Z"))
        controller.acceptGps(gps(40.009, "2026-09-02T13:02:01Z"))

        assertEquals(Instant.parse("2026-09-02T13:02:00Z"), checkpoints.get("walk-persist-time")?.lastObservedAt)
    }

    @Test
    fun persistentControllerResumePublishesCheckpointedWalkingState() {
        val route = route()
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val plan = WalkingPlanFactory.create(route, "walk-persistent", 0.0, 1.0)
        val water = waterApoi()

        val firstStore = AppStateStore()
        val firstRuntime = WalkingSessionRuntime(route, service, listOf(water))
        val firstController = WalkingAppStateController(
            route = route,
            walk = plan,
            catalog = catalog(water),
            store = firstStore,
            sessionRuntime = firstRuntime
        )
        firstController.start(RoutePosition("route", 0.0, 0.0), Instant.parse("2026-09-03T09:00:00Z"))
        val moved = firstController.acceptGps(gps(40.0045, "2026-09-03T09:02:00Z"))

        val resumedStore = AppStateStore()
        val resumedRuntime = WalkingSessionRuntime(route, service, listOf(water))
        val resumedController = WalkingAppStateController(
            route = route,
            walk = plan,
            catalog = catalog(water),
            store = resumedStore,
            sessionRuntime = resumedRuntime
        )
        val resumed = resumedController.resume(Instant.parse("2026-09-03T09:10:00Z"))

        assertEquals(moved.walking?.routePosition?.routeKm ?: -1.0, resumed.walking?.routePosition?.routeKm ?: -2.0, 0.001)
        assertEquals(moved.walking?.progress?.currentRouteKm ?: -1.0, resumed.walking?.progress?.currentRouteKm ?: -2.0, 0.001)
        assertEquals("water", resumed.walking?.nextApoi?.id)
        assertNotNull(resumedStore.state.walking)
        assertEquals(resumed, resumedStore.state)
    }

    @Test
    fun rejectedGpsPreservesPublishedWalkingState() {
        val store = AppStateStore()
        val controller = controller(catalog(waterApoi()), store)
        controller.start(RoutePosition("route", 0.0, 0.0), Instant.parse("2026-09-02T14:00:00Z"))
        val reliable = controller.acceptGps(gps(40.00225, "2026-09-02T14:02:00Z"))
        val before = store.state

        val after = controller.acceptGps(gps(40.009, "2026-09-02T14:02:01Z"))

        assertEquals(before.walking?.routePosition, after.walking?.routePosition)
        assertEquals(before.walking?.nextApoi, after.walking?.nextApoi)
        assertEquals(before.walking?.nextApoiDistanceKm, after.walking?.nextApoiDistanceKm)
        assertEquals(reliable.walking?.gpsState, after.walking?.gpsState)
        assertSame(after, store.state)
    }

    @Test
    fun signalLossPreservesPublishedReliablePositionAndApoi() {
        val store = AppStateStore()
        val controller = controller(catalog(waterApoi()), store)
        controller.start(RoutePosition("route", 0.0, 0.0), Instant.parse("2026-09-02T15:00:00Z"))
        val moving = controller.acceptGps(gps(40.00225, "2026-09-02T15:02:00Z"))

        val noSignal = controller.markNoSignal(Instant.parse("2026-09-02T15:02:31Z"))

        assertEquals(com.caminhos2027.v1.core.route.GpsState.NO_SIGNAL, noSignal.walking?.gpsState)
        assertEquals(moving.walking?.routePosition, noSignal.walking?.routePosition)
        assertEquals(moving.walking?.nextApoi, noSignal.walking?.nextApoi)
        assertEquals(moving.walking?.nextApoiDistanceKm, noSignal.walking?.nextApoiDistanceKm)
    }

    private fun controller(
        catalog: PublishedApoiCatalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() })),
        store: AppStateStore = AppStateStore()
    ) = WalkingAppStateController(
        route = route(),
        walk = WalkingPlanFactory.create(route(), "walk", 0.0, 1.0),
        catalog = catalog,
        store = store
    )

    private fun catalog(vararg records: Apoi) = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun gps(latitude: Double, capturedAt: String) = RawGpsPosition(latitude, -8.0, 5.0, Instant.parse(capturedAt))

    private fun waterApoi() = Apoi(
        id = "water", name = "Fonte", description = "APOI fictício de teste", mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0063, -8.0, LocationPrecision.EXACT, "SR", "SR", "Fixture", "route", 0.7, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, "SR test data")
    )

    private fun route() = Route(
        id = "route", name = "Route", officialName = "Route", totalDistanceKm = 1.0, source = "test", updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0045, -8.0), GeoPoint(40.009, -8.0))), stages = emptyList()
    )
}
