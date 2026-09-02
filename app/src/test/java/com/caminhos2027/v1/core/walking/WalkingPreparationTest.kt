package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Test

class WalkingPreparationTest {
    private val route = Route(
        id = "route", name = "SR", officialName = "SR", totalDistanceKm = 10.0,
        source = "SR", updatedAt = "2026-09-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.09, -8.0))),
        stages = listOf(
            Stage("s1", "route", 1, "Stage 1", 0.0, 5.0, 5.0, "A", "B", "SR"),
            Stage("s2", "route", 2, "Stage 2", 5.0, 10.0, 5.0, "B", "C", "SR")
        )
    )

    private fun apoi(id: String, km: Double, status: PublicationStatus = PublicationStatus.PUBLISHED) = Apoi(
        id, id, null, ApoiCategory.AGUA, setOf(ApoiCategory.AGUA),
        ApoiLocation(null, null, LocationPrecision.UNKNOWN, null, null, null, "route", km, null, null, RouteRelation.ON_ROUTE),
        ApoiPublication(status, null)
    )

    @Test
    fun preparationContainsIntersectedStagesAndApoiInsidePlan() {
        val walk = WalkingPlanFactory.create(route, "walk", 2.0, 8.0)
        val preparation = WalkingPreparationBuilder.build(
            route, walk, listOf(apoi("a1", 3.0), apoi("a2", 7.0), apoi("a3", 9.0))
        )

        assertEquals(listOf("s1", "s2"), preparation.stages.map { it.id })
        assertEquals(listOf("a1", "a2"), preparation.relevantApoi.map { it.id })
    }

    @Test
    fun preparationExcludesNonPublishedApoi() {
        val walk = WalkingPlanFactory.create(route, "walk", 2.0, 8.0)
        val preparation = WalkingPreparationBuilder.build(
            route, walk, listOf(apoi("a1", 3.0, PublicationStatus.HISTORICAL))
        )

        assertEquals(emptyList<Apoi>(), preparation.relevantApoi)
    }
}
