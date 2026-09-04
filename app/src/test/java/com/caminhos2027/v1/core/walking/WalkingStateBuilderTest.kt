package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingStateBuilderTest {
    @Test
    fun buildsSingleStateWithoutDuplicatingDerivedLogic() {
        val route = fixture()
        val walk = Walk("walk-1", route.id, plannedStartKm = 0.0, plannedDestinationKm = 2.0)
        val position = com.caminhos2027.v1.core.model.RoutePosition(route.id, 0.5, 5.0, "stage-1")
        val state = WalkingStateBuilder.build(route, walk, GpsState.ON_ROUTE, position, listOf(apoi()))

        assertEquals(0.5, state.progress?.currentRouteKm ?: -1.0, 0.001)
        assertEquals(1.5, state.progress?.remainingKm ?: -1.0, 0.001)
        assertEquals("water-1", state.nextApoi?.id)
        assertEquals(0.3, state.nextApoiDistanceKm ?: -1.0, 0.001)
        assertEquals(GpsState.ON_ROUTE, state.gpsState)
        assertNotNull(state.routePosition)
        assertTrue(!state.isOffline)
    }

    @Test
    fun nullPositionProducesStateWithoutFabricatedProgressOrApoi() {
        val route = fixture()
        val walk = Walk("walk-null", route.id, plannedStartKm = 0.0, plannedDestinationKm = 2.0)

        val state = WalkingStateBuilder.build(route, walk, GpsState.NO_SIGNAL, null, listOf(apoi()))

        assertNull(state.routePosition)
        assertNull(state.progress)
        assertNull(state.nextApoi)
        assertNull(state.nextApoiDistanceKm)
        assertEquals(GpsState.NO_SIGNAL, state.gpsState)
    }

    @Test
    fun preservesOfflineFlagWithoutChangingWalkingCalculations() {
        val route = fixture()
        val walk = Walk("walk-1", route.id, plannedStartKm = 0.0, plannedDestinationKm = 2.0)
        val position = com.caminhos2027.v1.core.model.RoutePosition(route.id, 0.5, 5.0, "stage-1")
        val state = WalkingStateBuilder.build(route, walk, GpsState.NO_SIGNAL, position, listOf(apoi()), offline = true)

        assertTrue(state.isOffline)
        assertEquals(0.5, state.progress?.currentRouteKm ?: -1.0, 0.001)
        assertEquals("water-1", state.nextApoi?.id)
    }

    private fun apoi() = Apoi(
        id = "water-1",
        name = "TEST/FICTITIOUS water",
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(null, null, LocationPrecision.LOCALITY_ONLY, "TEST", null, null, "test-route", 0.8, 10.0, null, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )

    private fun fixture() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 2.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(listOf(com.caminhos2027.v1.core.model.GeoPoint(40.0, -8.0), com.caminhos2027.v1.core.model.GeoPoint(40.0, -7.98))),
        stages = listOf(Stage("stage-1", "test-route", 1, "TEST/FICTITIOUS", 0.0, 2.0, 2.0, "A", "B", "TEST/FICTITIOUS"))
    )
}
