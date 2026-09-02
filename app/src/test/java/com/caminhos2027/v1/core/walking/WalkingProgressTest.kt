package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingProgressTest {
    @Test
    fun calculatesProgressFromActualStartAndPlannedDestination() {
        val route = fixture()
        val walk = Walk(
            id = "walk-1",
            routeId = "test-route",
            plannedStartKm = 2.0,
            plannedDestinationKm = 8.0,
            actualStartKm = 2.5
        )

        val progress = WalkingProgressCalculator.calculate(route, walk, 5.0)

        assertEquals(5.0, progress.routeKm, 0.001)
        assertEquals(2.5, progress.distanceFromStartKm ?: -1.0, 0.001)
        assertEquals(3.0, progress.distanceRemainingKm ?: -1.0, 0.001)
        assertEquals("stage-2", progress.stage?.id)
    }

    @Test
    fun currentRoutePositionIsClampedToOfficialRoute() {
        val route = fixture()
        val walk = Walk(id = "walk-1", routeId = "test-route")

        val progress = WalkingProgressCalculator.calculate(route, walk, 12.0)

        assertEquals(10.0, progress.routeKm, 0.001)
        assertEquals(0.0, progress.distanceRemainingKm ?: -1.0, 0.001)
        assertEquals("stage-2", progress.stage?.id)
    }

    @Test
    fun stageBoundaryBelongsToNextStage() {
        val stages = fixture().stages
        assertEquals("stage-2", WalkingProgressCalculator.findStage(stages, 5.0)?.id)
        assertEquals("stage-2", WalkingProgressCalculator.findStage(stages, 10.0)?.id)
    }

    @Test
    fun gapBetweenStagesDoesNotInventStage() {
        val stages = listOf(
            fixture().stages[0],
            fixture().stages[1].copy(startRouteKm = 6.0)
        )
        assertNull(WalkingProgressCalculator.findStage(stages, 5.5))
    }

    private fun fixture() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 10.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0, -7.9))),
        stages = listOf(
            Stage("stage-1", "test-route", 1, "TEST stage 1", 0.0, 5.0, 5.0, "Start", "Middle", "TEST/FICTITIOUS"),
            Stage("stage-2", "test-route", 2, "TEST stage 2", 5.0, 10.0, 5.0, "Middle", "End", "TEST/FICTITIOUS")
        )
    )
}
