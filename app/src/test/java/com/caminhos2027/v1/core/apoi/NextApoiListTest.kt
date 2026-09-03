package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class NextApoiListTest {
    private fun apoi(
        id: String,
        km: Double,
        status: PublicationStatus = PublicationStatus.PUBLISHED,
        relation: RouteRelation = RouteRelation.ON_ROUTE,
        availability: ApoiAvailabilityStatus = ApoiAvailabilityStatus.AWAITING_CONFIRMATION
    ) = Apoi(
        id,
        id,
        null,
        ApoiCategory.AGUA,
        emptySet(),
        ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", km, 0.0, 0.0, relation),
        ApoiPublication(status, null),
        availability = ApoiAvailability(status = availability)
    )

    @Test fun ordersByDistanceAndExcludesNonPublishable() {
        val result = NextApoiList.findAhead(listOf(
            apoi("far", 5.0), apoi("near", 2.0), apoi("past", 0.5), apoi("warning", 3.0, PublicationStatus.PUBLISHED_WITH_WARNING),
            apoi("historical", 4.0, PublicationStatus.HISTORICAL), apoi("distant", 2.5, relation = RouteRelation.DISTANT_POTENTIAL_SUPPORT)
        ), "route", 1.0)
        assertEquals(listOf("near", "warning", "far"), result.map { it.apoi.id })
        assertEquals(1.0, result.first().distanceKm, .001)
    }

    @Test fun excludesHistoricalExpiredAndClosedAvailability() {
        val result = NextApoiList.findAhead(listOf(
            apoi("historical", 1.5, availability = ApoiAvailabilityStatus.HISTORICAL),
            apoi("expired", 2.0, availability = ApoiAvailabilityStatus.EXPIRED),
            apoi("closed", 2.5, availability = ApoiAvailabilityStatus.CLOSED),
            apoi("reviewable", 3.0, availability = ApoiAvailabilityStatus.AWAITING_CONFIRMATION),
            apoi("recurring", 4.0, availability = ApoiAvailabilityStatus.RECURRING)
        ), "route", 1.0)

        assertEquals(listOf("reviewable", "recurring"), result.map { it.apoi.id })
    }

    @Test fun limitIsAppliedAfterOrdering() {
        val result = NextApoiList.findAhead((1..10).map { apoi("a$it", it.toDouble()) }, "route", 0.0, 3)
        assertEquals(listOf("a1", "a2", "a3"), result.map { it.apoi.id })
    }
}
