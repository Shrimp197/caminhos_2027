package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RouteGeometry
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteValidatorGeometryLengthTest {
    @Test
    fun geometryGrosslyDifferentFromDeclaredDistanceIsRejected() {
        val route = RouteValidatorTestFixture.route(
            totalDistanceKm = 20.0,
            geometry = RouteGeometry(
                listOf(
                    GeoPoint(40.0, -8.0),
                    GeoPoint(40.0, -7.99)
                )
            )
        )

        val errors = RouteValidator.validate(route)
        assertTrue(errors.any { it.contains("geometry length") })
    }
}

private object RouteValidatorTestFixture {
    fun route(totalDistanceKm: Double, geometry: RouteGeometry) =
        com.caminhos2027.v1.core.model.Route(
            id = "test-route",
            name = "TEST/FICTITIOUS route",
            officialName = "TEST/FICTITIOUS route",
            totalDistanceKm = totalDistanceKm,
            source = "TEST/FICTITIOUS",
            updatedAt = "2026-09-02",
            geometry = geometry,
            stages = emptyList()
        )
}
