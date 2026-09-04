package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteValidatorTest {
    @Test
    fun validRouteHasNoErrors() {
        val route = fixture()
        assertTrue(RouteValidator.validate(route).isEmpty())
    }

    @Test
    fun stageWithDifferentRouteIdIsRejected() {
        val route = fixture().copy(stages = listOf(fixture().stages.first().copy(routeId = "other-route")))
        assertFalse(RouteValidator.validate(route).isEmpty())
    }

    @Test
    fun stageDistanceInconsistentWithRouteIntervalIsRejected() {
        val route = fixture().copy(stages = listOf(fixture().stages.first().copy(distanceKm = 0.5)))
        assertFalse(RouteValidator.validate(route).isEmpty())
    }

    @Test
    fun geometryWithOnlyOnePointIsRejected() {
        val route = fixture().copy(geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0))))
        assertTrue(RouteValidator.validate(route).any { it.contains("at least two points") })
    }

    @Test
    fun geometryWithInvalidLatitudeIsRejected() {
        val route = fixture().copy(
            geometry = RouteGeometry(
                listOf(GeoPoint(91.0, -8.0), GeoPoint(40.0, -7.98827))
            )
        )
        assertTrue(RouteValidator.validate(route).any { it.contains("latitude") })
    }

    @Test
    fun geometryWithInvalidLongitudeIsRejected() {
        val route = fixture().copy(
            geometry = RouteGeometry(
                listOf(GeoPoint(40.0, -181.0), GeoPoint(40.0, -7.98827))
            )
        )
        assertTrue(RouteValidator.validate(route).any { it.contains("longitude") })
    }

    @Test
    fun consecutiveDuplicateGeometryPointIsRejected() {
        val point = GeoPoint(40.0, -8.0)
        val route = fixture().copy(geometry = RouteGeometry(listOf(point, point)))
        assertTrue(RouteValidator.validate(route).any { it.contains("duplicates previous point") })
    }

    @Test
    fun nonFiniteRouteDistanceIsRejected() {
        val route = fixture().copy(totalDistanceKm = Double.NaN)
        assertTrue(RouteValidator.validate(route).any { it.contains("totalDistanceKm") })
    }

    @Test
    fun nonFiniteStageMetricsAreRejected() {
        val stage = fixture().stages.first().copy(
            startRouteKm = Double.NaN,
            endRouteKm = Double.POSITIVE_INFINITY,
            distanceKm = Double.NaN
        )
        val route = fixture().copy(stages = listOf(stage))
        val errors = RouteValidator.validate(route)
        assertTrue(errors.any { it.contains("startRouteKm") && it.contains("finite") })
        assertTrue(errors.any { it.contains("endRouteKm") && it.contains("finite") })
        assertTrue(errors.any { it.contains("distanceKm") && it.contains("finite") })
    }

    private fun fixture() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 1.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.0, -7.98827)
            )
        ),
        stages = listOf(
            Stage(
                id = "test-stage-1",
                routeId = "test-route",
                number = 1,
                name = "TEST/FICTITIOUS stage",
                startRouteKm = 0.0,
                endRouteKm = 1.0,
                distanceKm = 1.0,
                startName = "TEST/FICTITIOUS start",
                endName = "TEST/FICTITIOUS end",
                source = "TEST/FICTITIOUS"
            )
        )
    )
}
