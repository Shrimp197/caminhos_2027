package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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
        val route = fixture().copy(stages = listOf(fixture().stages.first().copy(distanceKm = 9.0)))
        assertFalse(RouteValidator.validate(route).isEmpty())
    }

    private fun fixture() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 20.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.1, -8.1)
            )
        ),
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
