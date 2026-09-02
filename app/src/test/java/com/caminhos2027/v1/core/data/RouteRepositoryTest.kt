package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertSame
import org.junit.Test

class RouteRepositoryTest {
    @Test
    fun repositoryReturnsValidatedRoute() {
        val route = testRoute()
        val repository = RouteRepository(object : RouteDataSource {
            override fun loadRoute(): Route = route
        })

        assertSame(route, repository.getRoute())
    }

    @Test(expected = IllegalArgumentException::class)
    fun repositoryRejectsInvalidRoute() {
        val invalid = testRoute().copy(totalDistanceKm = 0.0)
        RouteRepository(object : RouteDataSource {
            override fun loadRoute(): Route = invalid
        }).getRoute()
    }

    private fun testRoute() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 20.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        stages = listOf(
            Stage(
                id = "test-stage-1",
                routeId = "test-route",
                number = 1,
                name = "TEST/FICTITIOUS stage",
                startRouteKm = 0.0,
                endRouteKm = 10.0,
                distanceKm = 10.0,
                startName = "TEST/FICTITIOUS start",
                endName = "TEST/FICTITIOUS end",
                source = "TEST/FICTITIOUS"
            )
        )
    )
}
