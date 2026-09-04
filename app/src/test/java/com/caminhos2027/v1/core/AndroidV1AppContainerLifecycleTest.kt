package com.caminhos2027.v1.core

import com.caminhos2027.v1.core.apoi.ApoiBrowser
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Objective
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.walking.InMemoryWalkRepository
import com.caminhos2027.v1.core.walking.InMemoryWalkingStateRepository
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import com.caminhos2027.v1.core.walking.WalkingSessionService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Integration-level regressions for Android composition recreation and session isolation. */
class AndroidV1AppContainerLifecycleTest {
    private val route = Route(
        id = "published",
        name = "published",
        officialName = "published",
        totalDistanceKm = 1.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0))),
        stages = emptyList()
    )

    @Test
    fun sameActiveWalkCanBeReattachedAcrossIndependentContainerInstances() {
        val repositories = Repositories()
        val active = repositories.start("walk-1", 0.25)
        val first = AndroidV1AppContainer(repositories.base())
        val second = AndroidV1AppContainer(repositories.base())

        val firstController = first.attachWalk(active)
        val secondController = second.attachWalk(active)

        assertNotSame(firstController, secondController)
        assertEquals("walk-1", first.store.state.walking?.walk?.id)
        assertEquals("walk-1", second.store.state.walking?.walk?.id)
        assertEquals("walk-1", first.runtime.activeWalk()?.id)
        assertEquals("walk-1", second.runtime.activeWalk()?.id)
    }

    @Test
    fun recreationRestoresPersistedCheckpointBeforePublishingState() {
        val repositories = Repositories()
        repositories.start("walk-1", 0.25)
        val recreated = AndroidV1AppContainer(repositories.base())

        val state = recreated.resumePersistedWalk(Instant.parse("2026-01-01T11:00:00Z"))

        assertEquals("walk-1", state.walking?.walk?.id)
        assertEquals(WalkStatus.ACTIVE, state.walking?.walk?.status)
        assertEquals(0.25, state.walking?.routePosition?.routeKm ?: -1.0, 0.0001)
    }

    @Test
    fun newContainerCannotAttachDifferentWalkWhileAnotherSessionIsPersistentlyActive() {
        val repositories = Repositories()
        repositories.start("walk-1", 0.25)
        val recreated = AndroidV1AppContainer(repositories.base())

        val error = try {
            recreated.attachWalk(walk("walk-2", WalkStatus.PLANNED))
            null
        } catch (expected: IllegalArgumentException) {
            expected
        }

        assertTrue(error?.message.orEmpty().contains("different walk"))
        assertNull(recreated.store.state.walking)
        assertEquals("walk-1", recreated.runtime.activeWalk()?.id)
    }

    @Test
    fun clearSessionRemovesCompositionSlicesButKeepsPersistentActiveSession() {
        val repositories = Repositories()
        val container = AndroidV1AppContainer(repositories.base())
        repositories.start("walk-1", 0.25)
        container.resumePersistedWalk(Instant.parse("2026-01-01T11:00:00Z"))
        container.store.setObjective(Objective("obj-1", route.id, "dest"))
        container.store.browseApoi(ApoiBrowser(repositories.catalog))
        container.store.buildDecision(route, emptyList())

        container.clearSession()

        assertNull(container.store.state.walking)
        assertNull(container.store.state.apoiBrowser)
        assertNull(container.store.state.decision)
        assertEquals("obj-1", container.store.state.objective?.id)
        assertEquals("walk-1", container.runtime.activeWalk()?.id)
    }

    @Test
    fun completedSessionAllowsAttachingANewWalkAfterClear() {
        val repositories = Repositories()
        val active = repositories.start("walk-1", 0.25)
        repositories.service.stop("walk-1", position(0.75), Instant.parse("2026-01-01T10:00:00Z"))
        val container = AndroidV1AppContainer(repositories.base())
        container.attachWalk(active.copy(status = WalkStatus.COMPLETED))
        container.clearSession()

        val newController = container.attachWalk(walk("walk-2", WalkStatus.PLANNED))

        assertEquals("walk-2", container.store.state.walking?.walk?.id)
        assertSame(newController, container.activeController())
        assertNull(container.runtime.activeWalk())
    }

    private fun walk(id: String, status: WalkStatus): Walk =
        Walk(
            id = id,
            routeId = route.id,
            plannedStartKm = 0.0,
            plannedDestinationKm = 1.0,
            status = status
        )

    private fun position(routeKm: Double) = RoutePosition(
        routeId = route.id,
        routeKm = routeKm,
        distanceToRouteMeters = 0.0
    )

    private inner class Repositories {
        val walkRepository = InMemoryWalkRepository()
        val stateRepository = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walkRepository, stateRepository)
        val catalog = PublishedApoiCatalog(ApoiRepository { emptyList() })

        fun start(id: String, routeKm: Double): Walk {
            service.prepare(walk(id, WalkStatus.PLANNED))
            return service.start(id, position(routeKm), Instant.parse("2026-01-01T09:00:00Z"))
        }

        fun runtime(): WalkingSessionRuntime =
            WalkingSessionRuntime(route, service, catalog.all())

        fun base(): V1AppContainer =
            V1AppContainer(
                route = route,
                catalog = catalog,
                appStateStore = AppStateStore(),
                sessionRuntime = runtime()
            )
    }
}
