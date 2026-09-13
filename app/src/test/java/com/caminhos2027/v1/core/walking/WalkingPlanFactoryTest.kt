package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class WalkingPlanFactoryTest {
    private val route = Route(
        id = "test-route",
        name = "Rota de teste",
        officialName = "Rota oficial de teste",
        totalDistanceKm = 10.0,
        source = "SR",
        updatedAt = "2026-09-01",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.09, -8.0)
            )
        ),
        stages = listOf(
            Stage("stage-1", "test-route", 1, "Etapa 1", 0.0, 4.0, 4.0, "A", "B", "SR"),
            Stage("stage-2", "test-route", 2, "Etapa 2", 4.0, 7.0, 3.0, "B", "C", "SR"),
            Stage("stage-3", "test-route", 3, "Etapa 3", 7.0, 10.0, 3.0, "C", "D", "SR")
        )
    )

    @Test
    fun createsPlanAndDerivesIntersectingOfficialStages() {
        val walk = WalkingPlanFactory.create(route, "walk-1", 2.0, 8.0)

        assertEquals("test-route", walk.routeId)
        assertEquals(2.0, walk.plannedStartKm!!, 0.001)
        assertEquals(8.0, walk.plannedDestinationKm!!, 0.001)
        assertEquals(listOf("stage-1", "stage-2", "stage-3"), walk.stageIds)
        assertEquals(WalkStatus.PLANNED, walk.status)
    }

    @Test
    fun aPlanInsideOneStageKeepsOnlyThatStage() {
        val walk = WalkingPlanFactory.create(route, "walk-2", 4.5, 6.5)

        assertEquals(listOf("stage-2"), walk.stageIds)
    }

    @Test
    fun routeEndpointsAreValidPlanBoundaries() {
        val walk = WalkingPlanFactory.create(route, "walk-endpoints", 0.0, 10.0)

        assertEquals(0.0, walk.plannedStartKm!!, 0.0)
        assertEquals(10.0, walk.plannedDestinationKm!!, 0.0)
        assertEquals(listOf("stage-1", "stage-2", "stage-3"), walk.stageIds)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankWalkId() {
        WalkingPlanFactory.create(route, "   ", 2.0, 8.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonFiniteStart() {
        WalkingPlanFactory.create(route, "walk-nan-start", Double.NaN, 8.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInfiniteDestination() {
        WalkingPlanFactory.create(route, "walk-infinite-destination", 2.0, Double.POSITIVE_INFINITY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDestinationBeforeStart() {
        WalkingPlanFactory.create(route, "walk-3", 6.0, 5.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPositionOutsideRoute() {
        WalkingPlanFactory.create(route, "walk-4", -0.1, 2.0)
    }
}
