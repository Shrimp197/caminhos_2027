package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import kotlin.test.Test
import kotlin.test.assertEquals

class GpxSimulationStartIndexTest {
    @Test
    fun choosesThePointNearestToThePlannedRouteKilometre() {
        val route = route()

        assertEquals(1, GpxSimulationStartIndex.nearestPointIndex(route, 1.1))
        assertEquals(2, GpxSimulationStartIndex.nearestPointIndex(route, 2.9))
    }

    @Test
    fun clampsPlannedStartOutsideTheRouteRange() {
        val route = route()

        assertEquals(0, GpxSimulationStartIndex.nearestPointIndex(route, -5.0))
        assertEquals(3, GpxSimulationStartIndex.nearestPointIndex(route, 99.0))
    }

    private fun route() = Route(
        id = "qa",
        name = "QA",
        officialName = "QA",
        totalDistanceKm = 3.3,
        source = "test",
        updatedAt = null,
        geometry = RouteGeometry(
            listOf(
                GeoPoint(41.1000, -8.5800),
                GeoPoint(41.1090, -8.5800),
                GeoPoint(41.1180, -8.5800),
                GeoPoint(41.1270, -8.5800)
            )
        ),
        stages = emptyList()
    )
}
