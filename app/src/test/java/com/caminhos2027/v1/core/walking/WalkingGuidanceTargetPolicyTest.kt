package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingGuidanceTargetPolicyTest {
    private val start = GeoPoint(41.1496, -8.6109)
    private val lastKnown = GeoPoint(41.1600, -8.6200)

    @Test
    fun pendingStartGuidesToPlannedStartWhenOutsideRoute() {
        val target = WalkingGuidanceTargetPolicy.forPendingStart(
            currentDistanceToRouteMeters = 120.0,
            possibleDeviationMeters = 35.0,
            plannedStart = start,
            routeName = "Caminho do Centenário"
        )

        assertEquals(start, target?.point)
        assertEquals("Início da caminhada · Caminho do Centenário", target?.label)
    }

    @Test
    fun pendingStartDoesNotGuideWhenAlreadyOnRoute() {
        val target = WalkingGuidanceTargetPolicy.forPendingStart(
            currentDistanceToRouteMeters = 20.0,
            possibleDeviationMeters = 35.0,
            plannedStart = start,
            routeName = "Caminho do Centenário"
        )

        assertNull(target)
    }

    @Test
    fun activeWalkGuidesBackToLastKnownRoutePoint() {
        val target = WalkingGuidanceTargetPolicy.forActiveWalk(
            currentDistanceToRouteMeters = 250.0,
            possibleDeviationMeters = 35.0,
            lastKnownOnRoute = lastKnown
        )

        assertEquals(lastKnown, target?.point)
        assertEquals("Regressar ao último ponto conhecido no Caminho", target?.label)
    }

    @Test
    fun activeWalkDoesNotInventTargetWithoutKnownRoutePoint() {
        val target = WalkingGuidanceTargetPolicy.forActiveWalk(
            currentDistanceToRouteMeters = 250.0,
            possibleDeviationMeters = 35.0,
            lastKnownOnRoute = null
        )

        assertNull(target)
    }
}
