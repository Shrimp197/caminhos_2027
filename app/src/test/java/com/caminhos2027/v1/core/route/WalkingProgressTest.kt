package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingProgressTest {
    @Test
    fun calculatesProgressFromActualStartToPlannedDestination() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(), 6.0)

        assertEquals(6.0, progress.currentRouteKm, 0.001)
        assertEquals(1.0, progress.walkedKm, 0.001)
        assertEquals(4.0, progress.remainingKm, 0.001)
        assertEquals(0.2, progress.progressRatio, 0.001)
        assertEquals("stage-2", progress.stageId)
    }

    @Test
    fun reachingTargetClampsProgressToOne() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(), 11.0)
        assertEquals(1.0, progress.progressRatio, 0.001)
        assertEquals(0.0, progress.remainingKm, 0.001)
        assertEquals("stage-2", progress.stageId)
    }

    @Test
    fun positionBeforeStartDoesNotCreateNegativeWalkedDistance() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(), 4.0)
        assertEquals(0.0, progress.walkedKm, 0.001)
        assertTrue(progress.remainingKm > 0.0)
        assertEquals("stage-1", progress.stageId)
    }

    @Test
    fun stageIsDerivedFromRoutePosition() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(), 6.0)
        assertEquals("stage-2", progress.stageId)
    }

    @Test
    fun gapsDoNotInventStage() {
        val progress = WalkingProgressCalculator.calculate(routeWithGap(), walk(), 6.5)
        assertNull(progress.stageId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mismatchedRouteIsRejected() {
        WalkingProgressCalculator.calculate(route(), walk().copy(routeId = "other"), 6.0)
    }

    private fun walk() = Walk(
        id = "walk-1",
        routeId = "test-route",
        plannedStartKm = 5.0,
        plannedDestinationKm = 10.0,
        actualStartKm = 5.0,
        status = WalkStatus.PLANNED
    )

    private fun route() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 12.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0, -7.9))),
        stages = listOf(
            stage("stage-1", 0.0, 6.0),
            stage("stage-2", 6.0, 12.0)
        )
    )

    private fun routeWithGap() = route().copy(
        stages = listOf(
            stage("stage-1", 0.0, 6.0),
            stage("stage-2", 7.0, 12.0)
        )
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
