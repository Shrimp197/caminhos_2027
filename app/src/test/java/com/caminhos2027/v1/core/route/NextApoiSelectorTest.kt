package com.caminhos2027.v1.core.route

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
import org.junit.Assert.assertNull
import org.junit.Test

class NextApoiSelectorTest {
    @Test
    fun selectsNearestPublishedApoiAheadByRouteKm() {
        val selected = NextApoiSelector.next(5.0, listOf(apoi("far", 9.0), apoi("near", 6.0)))
        assertEquals("near", selected?.id)
    }

    @Test
    fun excludesHistoricalAndDistantPotentialSupport() {
        val selected = NextApoiSelector.next(
            5.0,
            listOf(
                apoi("historical", 5.5, PublicationStatus.HISTORICAL),
                apoi("distant", 5.6, relation = RouteRelation.DISTANT_POTENTIAL_SUPPORT)
            )
        )
        assertNull(selected)
    }

    @Test
    fun excludesClosedExpiredAndHistoricalAvailability() {
        val selected = NextApoiSelector.next(
            5.0,
            listOf(
                apoi("closed", 5.5, availability = ApoiAvailabilityStatus.CLOSED),
                apoi("expired", 5.6, availability = ApoiAvailabilityStatus.EXPIRED),
                apoi("historical", 5.7, availability = ApoiAvailabilityStatus.HISTORICAL),
                apoi("current", 6.0, availability = ApoiAvailabilityStatus.CURRENT)
            )
        )
        assertEquals("current", selected?.id)
    }

    @Test
    fun canSelectNextApoiForSpecificService() {
        val selected = NextApoiSelector.next(
            5.0,
            listOf(apoi("water", 6.0, services = setOf(ApoiCategory.AGUA)), apoi("food", 5.5)),
            ApoiCategory.AGUA
        )
        assertEquals("water", selected?.id)
    }

    private fun apoi(
        id: String,
        routeKm: Double,
        status: PublicationStatus = PublicationStatus.PUBLISHED,
        relation: RouteRelation = RouteRelation.ON_ROUTE,
        services: Set<ApoiCategory> = setOf(ApoiCategory.DESCANSO),
        availability: ApoiAvailabilityStatus = ApoiAvailabilityStatus.CURRENT
    ) = Apoi(
        id = id,
        name = "TEST/FICTITIOUS APOI",
        description = null,
        mainCategory = services.first(),
        services = services,
        location = ApoiLocation(null, null, LocationPrecision.LOCALITY_ONLY, null, null, null, "test-route", routeKm, null, null, relation),
        publication = ApoiPublication(status, null),
        availability = ApoiAvailability(
            status = availability,
            validFrom = null,
            validUntil = null,
            recurrence = null,
            season = null,
            openingHours = null,
            notes = null
        )
    )
}
