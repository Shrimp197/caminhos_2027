package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingDecisionSupportTest {
    @Test fun presentsDistanceToPlannedDestinationWithoutChoosingForPilgrim() {
        val context = WalkingDecisionSupport.build(
            route(), Walk("walk", "route", 2.0, 10.0), position(4.0), emptyList()
        )

        assertEquals(6.0, context.remainingToPlannedDestinationKm, 0.001)
        assertEquals("stop-now", context.stopNow.id)
        assertEquals("continue-to-planned-destination", context.continueWalking.id)
        assertEquals(6.0, context.continueWalking.distanceKm, 0.001)
    }

    @Test fun continuationShowsOnlyPublishedApoiBetweenPositionAndDestination() {
        val ahead = apoi("ahead", 6.0, PublicationStatus.PUBLISHED)
        val warning = apoi("warning", 8.0, PublicationStatus.PUBLISHED_WITH_WARNING)
        val after = apoi("after", 12.0, PublicationStatus.PUBLISHED)
        val behind = apoi("behind", 3.0, PublicationStatus.PUBLISHED)
        val context = WalkingDecisionSupport.build(
            route(), Walk("walk", "route", 2.0, 10.0), position(4.0), listOf(ahead, warning, after, behind)
        )

        assertEquals(listOf("ahead", "warning"), context.continueWalking.relevantApoi.map { it.id })
    }

    @Test fun stopNowDoesNotInventAnApoiAtCurrentPosition() {
        val context = WalkingDecisionSupport.build(
            route(), Walk("walk", "route", 2.0, 10.0), position(4.0), listOf(apoi("near", 4.0, PublicationStatus.PUBLISHED))
        )

        assertTrue(context.stopNow.relevantApoi.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPositionFromAnotherRoute() {
        WalkingDecisionSupport.build(route(), Walk("walk", "route", 2.0, 10.0), position(4.0, "other"), emptyList())
    }

    private fun route() = Route(
        id = "route", name = "Teste", officialName = "Teste oficial", totalDistanceKm = 20.0,
        source = "test", updatedAt = "2027-01-01", geometry = RouteGeometry(emptyList()), stages = listOf(
            Stage("stage-1", "route", 1, "Etapa", 0.0, 20.0, 20.0, "A", "B", "test")
        )
    )

    private fun position(km: Double, routeId: String = "route") =
        RoutePosition(routeId, km, 0.0, "stage-1")

    private fun apoi(id: String, km: Double, status: PublicationStatus) = Apoi(
        id = id, name = id, description = null, mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(status, null)
    )
}
