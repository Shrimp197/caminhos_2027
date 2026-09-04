package com.caminhos2027.v1

import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.InMemoryWalkingStateRepository
import com.caminhos2027.v1.core.walking.InMemoryWalkRepository
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import com.caminhos2027.v1.core.walking.WalkingSessionService
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class V1AppContainerTest {
    @Test
    fun controllerPublishesThroughSharedStoreAndPersistentRuntime() {
        val route = fixtureRoute()
        val store = AppStateStore()
        val runtime = WalkingSessionRuntime(
            route = route,
            sessionService = WalkingSessionService(
                repository = InMemoryWalkRepository(),
                stateRepository = InMemoryWalkingStateRepository()
            ),
            publishedApoi = emptyList()
        )
        val container = V1AppContainer(
            route = route,
            catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() })),
            appStateStore = store,
            sessionRuntime = runtime
        )
        val walk = Walk(
            id = "walk-container",
            routeId = route.id,
            plannedStartKm = 0.0,
            plannedDestinationKm = 1.0
        )

        val state = container.controller(walk).start(
            position = RoutePosition(route.id, 0.0, 0.0, "stage-1"),
            now = Instant.parse("2026-09-04T08:00:00Z")
        )

        assertSame(state, store.state.walking)
        assertEquals("walk-container", state.walking?.walk?.id)
        assertEquals(0.0, state.walking?.routePosition?.routeKm ?: -1.0, 0.0001)
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
