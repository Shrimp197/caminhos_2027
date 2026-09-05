package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Test

class StageLocatorTest {
    @Test
    fun boundaryBelongsToNextStage() {
        val stage = StageLocator.currentStage(route(), 5.0)
        assertEquals("stage-2", stage?.id)
    }

    @Test
    fun gapBetweenStagesDoesNotInventStage() {
        val stage = StageLocator.currentStage(routeWithGap(), 5.5)
        assertEquals(null, stage)
    }

    @Test
    fun endOfLastStageStillBelongsToLastStage() {
        val stage = StageLocator.currentStage(route(), 10.0)
        assertEquals("stage-2", stage?.id)
    }

    @Test
    fun positionBeyondRouteEndIsClampedToLastStage() {
        val stage = StageLocator.currentStage(route(), 11.0)
        assertEquals("stage-2", stage?.id)
    }

    @Test
    fun emptyStageCatalogReturnsNoStage() {
        val stage = StageLocator.currentStage(route().copy(stages = emptyList()), 5.0)
        assertEquals(null, stage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeRoutePositionIsRejected() {
        StageLocator.currentStage(route(), -0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonFiniteRoutePositionIsRejected() {
        StageLocator.currentStage(route(), Double.POSITIVE_INFINITY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonFiniteRouteDistanceIsRejected() {
        StageLocator.currentStage(route().copy(totalDistanceKm = Double.NaN), 5.0)
    }

    private fun route() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 10.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0, -7.9))),
        stages = listOf(stage("stage-1", 0.0, 5.0), stage("stage-2", 5.0, 10.0))
    )

    private fun routeWithGap() = route().copy(
        stages = listOf(stage("stage-1", 0.0, 5.0), stage("stage-2", 6.0, 10.0))
    )

    private fun stage(id: String, start: Double, end: Double) = Stage(
        id = id,
        routeId = "test-route",
        number = if (id == "stage-1") 1 else 2,
        name = "TEST/FICTITIOUS stage",
        startRouteKm = start,
        endRouteKm = end,
        distanceKm = end - start,
        startName = "TEST/FICTITIOUS start",
        endName = "TEST/FICTITIOUS end",
        source = "TEST/FICTITIOUS"
    )
}
