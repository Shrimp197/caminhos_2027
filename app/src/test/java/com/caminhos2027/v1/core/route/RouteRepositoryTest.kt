package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RouteRepositoryTest {
    private val route = Route(
        id = "centenario-test",
        name = "Caminho de teste",
        officialName = "Caminho de teste",
        totalDistanceKm = 2.0,
        source = "test-fixture",
        updatedAt = "2026-09-01",
        geometry = RouteGeometry(listOf(GeoPoint(41.0, -8.0), GeoPoint(41.01, -8.0))),
        stages = emptyList()
    )

    @Test
    fun `catalog returns requested route`() {
        val catalog = RouteCatalog(InMemoryRouteRepository(listOf(route)))

        assertEquals(route, catalog.requireRoute("centenario-test"))
    }

    @Test
    fun `repository returns null for unknown route`() {
        val repository = InMemoryRouteRepository(listOf(route))

        assertNull(repository.getById("missing"))
    }
}
