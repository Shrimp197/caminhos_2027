package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingGuidanceTargetPolicyTest {
    private val start = GeoPoint(41.1496, -8.6109)
    private val lastKnown = GeoPoint(41.1600, -8.6200)
    private val route = Route(
        id = "test",
        name = "Teste",
        officialName = "Teste",
        totalDistanceKm = 5.0,
        source = "test",
        updatedAt = null,
        geometry = RouteGeometry(
            points = listOf(GeoPoint(40.0000, -8.0000), GeoPoint(40.0450, -8.0000))
        ),
        stages = emptyList()
    )

    @Test
    fun pendingStartGuidesToPlannedStartWhenOutsideRoute() {
        val target = WalkingGuidanceTargetPolicy.forPendingStart(
            currentDistanceToRouteMeters = 120.0,
            possibleDeviationMeters = 35.0,
            plannedStart = start,
            routeName = "Caminho do Centenário"
        )

        assertEquals(start, target?.point)
        assertEquals("Ir para o início da caminhada · Caminho do Centenário", target?.label)
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
    fun pointAtRouteKmInterpolatesAlongRoute() {
        val point = WalkingGuidanceTargetPolicy.pointAtRouteKm(route, 2.5)
        assertEquals(40.0225, point.latitude, 0.0005)
        assertEquals(-8.0000, point.longitude, 0.000001)
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
