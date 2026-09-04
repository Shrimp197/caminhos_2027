package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFailsWith
import org.junit.Test

class WalkingProgressCalculatorTest {
    @Test
    fun exactStartProducesZeroProgress() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = 2.0, plannedDestinationKm = 8.0), 2.0)

        assertEquals(2.0, progress.currentRouteKm, 0.000001)
        assertEquals(0.0, progress.walkedKm, 0.000001)
        assertEquals(6.0, progress.remainingKm, 0.000001)
        assertEquals(0.0, progress.progressRatio, 0.000001)
        assertEquals("stage-1", progress.stageId)
    }

    @Test
    fun exactDestinationProducesCompleteProgress() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = 2.0, plannedDestinationKm = 8.0), 8.0)

        assertEquals(8.0, progress.currentRouteKm, 0.000001)
        assertEquals(6.0, progress.walkedKm, 0.000001)
        assertEquals(0.0, progress.remainingKm, 0.000001)
        assertEquals(1.0, progress.progressRatio, 0.000001)
        assertEquals("stage-2", progress.stageId)
    }

    @Test
    fun positionBeforeStartDoesNotCreateNegativeWalkedDistance() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = 4.0, plannedDestinationKm = 8.0), 2.0)

        assertEquals(0.0, progress.walkedKm, 0.000001)
        assertEquals(6.0, progress.remainingKm, 0.000001)
        assertEquals(0.0, progress.progressRatio, 0.000001)
    }

    @Test
    fun positionBeyondRouteEndIsClampedWithoutExceedingCompletion() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = 2.0, plannedDestinationKm = 8.0), 50.0)

        assertEquals(10.0, progress.currentRouteKm, 0.000001)
        assertEquals(8.0, progress.walkedKm, 0.000001)
        assertEquals(0.0, progress.remainingKm, 0.000001)
        assertEquals(1.0, progress.progressRatio, 0.000001)
        assertEquals("stage-2", progress.stageId)
    }

    @Test
    fun actualStartOverridesPlannedStartForProgressBaseline() {
        val progress = WalkingProgressCalculator.calculate(
            route(),
            walk(plannedStartKm = 1.0, plannedDestinationKm = 8.0, actualStartKm = 3.0),
            5.0
        )

        assertEquals(2.0, progress.walkedKm, 0.000001)
        assertEquals(3.0, progress.remainingKm, 0.000001)
        assertEquals(2.0 / 5.0, progress.progressRatio, 0.000001)
    }

    @Test
    fun zeroLengthPlanIsReportedAsComplete() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = 5.0, plannedDestinationKm = 5.0), 5.0)

        assertEquals(0.0, progress.walkedKm, 0.000001)
        assertEquals(0.0, progress.remainingKm, 0.000001)
        assertEquals(1.0, progress.progressRatio, 0.000001)
    }

    @Test
    fun exactRouteEndIsAlsoCompleteWhenDestinationIsRouteEnd() {
        val progress = WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = 2.0, plannedDestinationKm = 10.0), 10.0)

        assertEquals(10.0, progress.currentRouteKm, 0.000001)
        assertEquals(8.0, progress.walkedKm, 0.000001)
        assertEquals(0.0, progress.remainingKm, 0.000001)
        assertEquals(1.0, progress.progressRatio, 0.000001)
        assertEquals("stage-2", progress.stageId)
    }

    @Test
    fun positionBetweenStagesDoesNotFabricateStageIdentity() {
        val progress = WalkingProgressCalculator.calculate(routeWithGap(), walk(plannedStartKm = 0.0, plannedDestinationKm = 10.0), 5.5)

        assertEquals(null, progress.stageId)
    }

    @Test
    fun routeMismatchIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(routeId = "other-route"), 2.0)
        }
    }

    @Test
    fun negativePositionIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(), -0.001)
        }
    }

    @Test
    fun nonFinitePositionIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(), Double.NaN)
        }
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(), Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun nonFiniteWalkPlanningValuesAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(plannedStartKm = Double.NaN), 2.0)
        }
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(plannedDestinationKm = Double.POSITIVE_INFINITY), 2.0)
        }
        assertFailsWith<IllegalArgumentException> {
            WalkingProgressCalculator.calculate(route(), walk(actualStartKm = Double.NEGATIVE_INFINITY), 2.0)
        }
    }

    private fun walk(
        routeId: String = "test-route",
        plannedStartKm: Double? = 0.0,
        plannedDestinationKm: Double? = 10.0,
        actualStartKm: Double? = null
    ) = Walk(
        id = "walk-1",
        routeId = routeId,
        plannedStartKm = plannedStartKm,
        plannedDestinationKm = plannedDestinationKm,
        actualStartKm = actualStartKm
    )

    private fun route() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 10.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-04",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.01, -8.0))),
        stages = listOf(
            Stage("stage-1", "test-route", 1, "Stage 1", 0.0, 5.0, 5.0, "A", "B", "TEST/FICTITIOUS"),
            Stage("stage-2", "test-route", 2, "Stage 2", 5.0, 10.0, 5.0, "B", "C", "TEST/FICTITIOUS")
        )
    )

    private fun routeWithGap() = route().copy(
        stages = listOf(
            Stage("stage-1", "test-route", 1, "Stage 1", 0.0, 5.0, 5.0, "A", "B", "TEST/FICTITIOUS"),
            Stage("stage-2", "test-route", 2, "Stage 2", 6.0, 10.0, 4.0, "B", "C", "TEST/FICTITIOUS")
        )
    )
}
