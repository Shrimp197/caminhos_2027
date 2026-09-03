package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteDatasetValidatorTest {
    @Test
    fun validRoutePassesValidation() {
        val route = fixtureRoute()

        val result = RouteDatasetValidator.validate(route)

        assertTrue(result.valid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun geometryWithOnePointIsRejected() {
        val route = fixtureRoute().copy(
            geometry = RouteGeometry(listOf(GeoPoint(41.0, -8.6)))
        )

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("at least two points") })
    }

    @Test
    fun nonFiniteGeometryIsRejectedByTheSharedValidator() {
        val route = fixtureRoute().copy(
            geometry = RouteGeometry(
                listOf(
                    GeoPoint(41.0, -8.60),
                    GeoPoint(Double.NaN, -8.60)
                )
            )
        )

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("finite") })
    }

    @Test
    fun missingSourceIsRejected() {
        val route = fixtureRoute().copy(source = "")

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("source") })
    }

    @Test
    fun geometryDistanceMismatchIsRejected() {
        val route = fixtureRoute().copy(totalDistanceKm = 50.0)

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("differs from declared") })
    }

    @Test
    fun stageWithWrongRouteIsRejected() {
        val route = fixtureRoute().copy(
            stages = listOf(fixtureRoute().stages.first().copy(routeId = "other-route"))
        )

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("routeId different") })
    }

    @Test
    fun duplicateStageIdsAreRejectedByTheSharedValidator() {
        val first = fixtureRoute().stages.first()
        val route = fixtureRoute().copy(stages = listOf(first, first.copy(number = 2)))

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("duplicate stage id") })
    }

    @Test
    fun overlappingStagesAreRejected() {
        val route = fixtureRoute().copy(
            stages = listOf(
                Stage("s1", "test-route", 1, "A", 0.0, 5.0, 5.0, "A", "B", "test"),
                Stage("s2", "test-route", 2, "B", 4.0, 10.0, 6.0, "B", "C", "test")
            )
        )

        val result = RouteDatasetValidator.validate(route)

        assertFalse(result.valid)
        assertTrue(result.errors.any { it.contains("out of route order") })
    }

    private fun fixtureRoute(): Route = Route(
        id = "test-route",
        name = "Test route",
        officialName = "Test route official",
        totalDistanceKm = 11.12,
        source = "synthetic-test-fixture",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(41.0, -8.60),
                GeoPoint(41.05, -8.60),
                GeoPoint(41.10, -8.60)
            )
        ),
        stages = listOf(
            Stage("s1", "test-route", 1, "A", 0.0, 5.56, 5.56, "A", "B", "test"),
            Stage("s2", "test-route", 2, "B", 5.56, 11.12, 5.56, "B", "C", "test")
        )
    )
}
