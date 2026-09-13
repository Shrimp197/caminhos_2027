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
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** End-to-end lifecycle invariants across runtime, persistence and the shared read model. */
class WalkingLifecyclePersistenceIntegrationTest {
    @Test
    fun startMoveLoseSignalRecreateRecoverAndStopKeepsOneConsistentSession() {
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val water = waterApoi()
        val plan = WalkingPlanFactory.create(route, "walk-e2e", 0.4, 1.8)
        val runtime = WalkingSessionRuntime(route, service, listOf(water))

        runtime.prepare(plan)
        val started = runtime.start("walk-e2e", start, t("08:00:00"))
        val moved = runtime.accept(gps(40.0045, "08:02:00"))
        val beforeLossKm = moved.routePosition!!.routeKm
        val beforeLossApoi = moved.nextApoi?.id

        val noSignal = runtime.markNoSignal(t("08:02:31"))
        assertEquals(GpsState.NO_SIGNAL, noSignal.gpsState)
        assertEquals(beforeLossKm, noSignal.routePosition!!.routeKm, 0.001)
        assertEquals(beforeLossApoi, noSignal.nextApoi?.id)

        val recreated = WalkingSessionRuntime(route, service, listOf(water))
        val resumed = recreated.resume(t("08:05:00"))
        assertNotNull(resumed)
        assertEquals(beforeLossKm, resumed!!.routePosition!!.routeKm, 0.001)
        assertEquals(beforeLossApoi, resumed.nextApoi?.id)
        assertEquals(GpsState.NO_SIGNAL, resumed.gpsState)

        val recovered = recreated.accept(gps(40.0063, "08:06:00"))
        assertEquals(GpsState.ON_ROUTE, recovered.gpsState)
        assertTrue(recovered.routePosition!!.routeKm > beforeLossKm)
        assertEquals("water-1", recovered.nextApoi?.id)

        val completed = recreated.stop(
            RoutePosition(route.id, recovered.routePosition.routeKm, 2.0),
            t("09:00:00")
        )
        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertNull(service.resume())
        assertNull(checkpoints.get("walk-e2e"))
        assertEquals(WalkStatus.COMPLETED, walks.getById("walk-e2e")!!.status)
        assertEquals(started.walk.id, completed.id)
    }

    @Test
    fun invalidStopCannotEraseCheckpointOrMakeAnActiveWalkDisappear() {
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-invalid-stop", 0.4, 1.8))
        runtime.start("walk-invalid-stop", start, t("10:00:00"))
        runtime.accept(gps(40.0045, "10:02:00"))
        val checkpointBefore = service.resumeCheckpoint("walk-invalid-stop")
        assertNotNull(checkpointBefore)

        try {
            runtime.stop(RoutePosition("wrong-route", 0.5, 2.0), t("10:03:00"))
        } catch (_: IllegalArgumentException) {
            // Expected: route validation must happen before service mutation.
        }

        assertEquals(WalkStatus.ACTIVE, walks.getById("walk-invalid-stop")!!.status)
        assertEquals(checkpointBefore, service.resumeCheckpoint("walk-invalid-stop"))
        assertNotNull(runtime.resume(t("10:03:01")))
    }

    @Test
    fun completedSessionCannotBeResurrectedByRuntimeRecreation() {
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-terminal", 0.4, 1.8))
        runtime.start("walk-terminal", start, t("11:00:00"))
        val completed = runtime.stop(RoutePosition(route.id, 1.4, 2.0), t("12:00:00"))

        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertNull(WalkingSessionRuntime(route, service, emptyList()).resume(t("12:01:00")))
        assertEquals(WalkStatus.COMPLETED, walks.getById("walk-terminal")!!.status)

        try {
            runtime.stop(RoutePosition(route.id, 1.4, 2.0), t("12:02:00"))
            throw AssertionError("completed session must reject a second stop")
        } catch (_: IllegalArgumentException) {
            // Expected terminal lifecycle rejection.
        }
    }

    @Test
    fun rejectedGpsDoesNotChangeSharedReadModelAfterRuntimeRecreation() {
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val water = waterApoi()
        val store = AppStateStore()
        val runtime = WalkingSessionRuntime(route, service, listOf(water))
        val controller = WalkingAppStateController(
            route = route,
            walk = WalkingPlanFactory.create(route, "walk-shared", 0.0, 1.0),
            catalog = catalog(water),
            store = store,
            sessionRuntime = runtime
        )

        controller.start(RoutePosition(route.id, 0.0, 0.0), t("13:00:00"))
        val reliable = controller.acceptGps(gps(40.00225, "13:02:00"))
        val before = store.state
        val beforeCheckpoint = service.resumeCheckpoint("walk-shared")

        controller.acceptGps(gps(40.009, "13:02:01"))
        val afterRejected = store.state

        assertEquals(before.walking!!.routePosition, afterRejected.walking!!.routePosition)
        assertEquals(before.walking!!.nextApoi, afterRejected.walking!!.nextApoi)
        assertEquals(reliable.walking!!.routePosition, afterRejected.walking!!.routePosition)
        assertEquals(beforeCheckpoint, service.resumeCheckpoint("walk-shared"))

        val recreatedStore = AppStateStore()
        val recreated = WalkingSessionRuntime(route, service, listOf(water))
            .resume(t("13:05:00"))
        assertNotNull(recreated)
        recreatedStore.setWalking(recreated)
        assertEquals(afterRejected.walking!!.routePosition, recreatedStore.state.walking!!.routePosition)
        assertEquals(afterRejected.walking!!.nextApoi, recreatedStore.state.walking!!.nextApoi)
    }

    @Test
    fun offlineFlagSurvivesResumeWithoutReplacingReliablePosition() {
        val walks = InMemoryWalkRepository()
        val checkpoints = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, checkpoints)
        val runtime = WalkingSessionRuntime(route, service, emptyList())
        runtime.prepare(WalkingPlanFactory.create(route, "walk-offline-resume", 0.4, 1.8))
        runtime.start("walk-offline-resume", start, t("14:00:00"))
        val moved = runtime.accept(gps(40.0045, "14:02:00"))
        val offline = runtime.setOffline(true)

        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume(t("14:05:00"))

        assertTrue(offline.isOffline)
        assertNotNull(resumed)
        assertTrue(resumed!!.isOffline)
        assertEquals(moved.routePosition, resumed.routePosition)
        assertEquals(moved.nextApoi, resumed.nextApoi)
    }

    private fun gps(latitude: Double, capturedAt: String) =
        RawGpsPosition(latitude, -8.0, 5.0, t(capturedAt))

    private fun t(clock: String) = Instant.parse("2026-09-04T${clock}Z")

    private fun catalog(vararg records: Apoi) =
        PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun waterApoi() = Apoi(
        id = "water-1",
        name = "TEST/FICTITIOUS water",
        description = "APOI fictício de teste",
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            40.0072, -8.0, LocationPrecision.EXACT,
            "TEST", "TEST", "Fixture", route.id, 0.8, 0.0, 0.0,
            RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, "TEST fixture")
    )

    companion object {
        private val route = Route(
            "sr-route", "SR", "SR synthetic route", 2.0, "SR", "2026-09-01",
            RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))),
            emptyList()
        )
        private val start = RoutePosition(route.id, 0.4, 3.0)
    }
}
