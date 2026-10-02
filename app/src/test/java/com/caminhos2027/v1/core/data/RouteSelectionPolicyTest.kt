package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteSelectionPolicyTest {
    private fun walk(id: String, routeId: String, status: WalkStatus) =
        Walk(id = id, routeId = routeId, plannedStartKm = 0.0, plannedDestinationKm = 10.0, status = status)

    @Test
    fun activeWalkWinsOverLastSelectedRoute() {
        val walks = listOf(
            walk("centenario-plan", "centenario", WalkStatus.PLANNED),
            walk("sr-active", "sr", WalkStatus.ACTIVE)
        )
        assertEquals("sr", RouteSelectionPolicy.preferredRouteId(walks, "centenario") { it in setOf("centenario", "sr") })
    }

    @Test
    fun lastSelectedRouteWinsWithoutActiveWalk() {
        val walks = listOf(
            walk("centenario-plan", "centenario", WalkStatus.PLANNED),
            walk("sr-plan", "sr", WalkStatus.PLANNED)
        )
        assertEquals("centenario", RouteSelectionPolicy.preferredRouteId(walks, "centenario") { it in setOf("centenario", "sr") })
    }

    @Test
    fun newestPlannedRouteIsOnlyFallbackForLegacyInstallations() {
        val walks = listOf(
            walk("old", "centenario", WalkStatus.PLANNED),
            walk("new", "hf", WalkStatus.PLANNED)
        )
        assertEquals("hf", RouteSelectionPolicy.preferredRouteId(walks, null) { it in setOf("centenario", "hf") })
    }

    @Test
    fun unavailableSelectedRouteDoesNotSelectAnotherPlanSilently() {
        val walks = listOf(
            walk("centenario-plan", "centenario", WalkStatus.PLANNED),
            walk("sr-plan", "sr", WalkStatus.PLANNED)
        )
        assertEquals(null, RouteSelectionPolicy.preferredRouteId(walks, "unknown") { it in setOf("centenario", "sr") })
    }
}
