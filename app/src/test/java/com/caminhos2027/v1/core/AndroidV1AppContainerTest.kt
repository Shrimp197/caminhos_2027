package com.caminhos2027.v1.core

import com.caminhos2027.v1.V1AppContainer
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.walking.InMemoryWalkRepository
import com.caminhos2027.v1.core.walking.InMemoryWalkingStateRepository
import com.caminhos2027.v1.core.walking.WalkingPreparationService
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import com.caminhos2027.v1.core.walking.WalkingSessionService
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class AndroidV1AppContainerTest {
    @Test
    fun resumePersistedWalkRebuildsControllerAndPublishesRestoredState() {
        val route = fixtureRoute()
        val runtime = WalkingSessionRuntime(
            route = route,
            sessionService = WalkingSessionService(
                repository = InMemoryWalkRepository(),
                stateRepository = InMemoryWalkingStateRepository()
            ),
            publishedApoi = emptyList()
        )
        val walk = Walk(
            id = "walk-recreated",
            routeId = route.id,
            plannedStartKm = 0.0,
            plannedDestinationKm = 1.0
        )

        runtime.prepare(walk)
        runtime.start(
            walkId = walk.id,
            position = RoutePosition(route.id, 0.0, 0.0, "stage-1"),
            now = Instant.parse("2026-09-04T08:00:00Z")
        )

        val recreated = container(route, runtime)
        val restored = recreated.resumePersistedWalk(Instant.parse("2026-09-04T08:05:00Z"))

        assertEquals(walk.id, restored.walking?.walk?.id)
        assertEquals(walk.id, recreated.store.state.walking?.walk?.id)
        assertNotNull(recreated.activeController())
        assertEquals("walk-recreated", restored.walking?.walk?.id)
        assertEquals(0.0, restored.walking?.routePosition?.routeKm ?: -1.0, 0.0001)
        assertNull(recreated.store.state.decision)
    }

    @Test
    fun resumePersistedWalkWithNoActiveSessionClearsCompositionState() {
        val route = fixtureRoute()
        val runtime = WalkingSessionRuntime(
            route = route,
            sessionService = WalkingSessionService(
                repository = InMemoryWalkRepository(),
                stateRepository = InMemoryWalkingStateRepository()
            ),
            publishedApoi = emptyList()
        )
        val container = container(route, runtime)

        val restored = container.resumePersistedWalk(Instant.parse("2026-09-04T08:05:00Z"))

        assertNull(restored.walking)
        assertNull(container.store.state.walking)
    }

    @Test
    fun restorePreparedWalkRebuildsPlannedStateWithoutStartingIt() {
        val route = fixtureRoute()
        val runtime = WalkingSessionRuntime(
            route = route,
            sessionService = WalkingSessionService(
                repository = InMemoryWalkRepository(),
                stateRepository = InMemoryWalkingStateRepository()
            ),
            publishedApoi = emptyList()
        )
        val preparationRepository = InMemoryWalkRepository()
        val first = container(route, runtime, preparationRepository)

        val preparation = first.preparationService!!.save(
            walkId = "walk-planned-recreated",
            startRouteKm = 0.0,
            destinationRouteKm = 1.0
        )
        assertEquals(WalkStatus.PLANNED, preparation.walk.status)

        val recreated = container(route, runtime, preparationRepository)
        val restored = recreated.restorePreparedWalk()

        assertNotNull(restored)
        assertEquals("walk-planned-recreated", restored?.walk?.id)
        assertEquals(WalkStatus.PLANNED, restored?.walk?.status)
        assertNull(recreated.store.state.walking)
        assertNull(runtime.activeWalk())
    }

    private fun container(
        route: Route,
        runtime: WalkingSessionRuntime,
        preparationRepository: InMemoryWalkRepository = InMemoryWalkRepository()
    ): AndroidV1AppContainer {
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() }))
        return AndroidV1AppContainer(
            V1AppContainer(
                route = route,
                catalog = catalog,
                appStateStore = AppStateStore(),
                sessionRuntime = runtime,
                preparationService = WalkingPreparationService(route, preparationRepository, catalog)
            )
        )
    }

    private fun fixtureRoute() = Route(
        id = "route-container",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 1.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-04",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.0045, -8.0)
            )
        ),
        stages = listOf(
            Stage("stage-1", "route-container", 1, "TEST/FICTITIOUS", 0.0, 1.0, 1.0, "Início", "Fim", "TEST/FICTITIOUS")
        )
    )
}
