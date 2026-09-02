package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatedRouteRepositoryTest {
    @Test
    fun exposesValidRoute() {
        val route = route("valid", 1.0)
        val repository = ValidatedRouteRepository(listOf(route))

        assertNotNull(repository.getById("valid"))
    }

    @Test
    fun hidesInvalidRoute() {
        val invalid = route("invalid", 100.0)
        val repository = ValidatedRouteRepository(listOf(invalid))

        assertNull(repository.getById("invalid"))
    }

    private fun route(id: String, declaredDistanceKm: Double) = Route(
        id = id,
        name = "Teste",
        officialName = "Teste",
        totalDistanceKm = declaredDistanceKm,
        source = "test",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0000, -8.0000),
                GeoPoint(40.0045, -8.0000),
                GeoPoint(40.0090, -8.0000)
            )
        ),
        stages = emptyList()
    )
}
