package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.apoi.ApoiBrowserState
import com.caminhos2027.v1.core.apoi.ApoiBrowserQuery
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiCharacteristics
import com.caminhos2027.v1.core.model.ApoiConfidence
import com.caminhos2027.v1.core.model.ApoiContact
import com.caminhos2027.v1.core.model.ApoiCost
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.ApoiReservation
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgress
import com.caminhos2027.v1.core.walking.WalkingState
import org.junit.Assert.assertEquals
import org.junit.Test

class WalkingExperienceScreenV1Test {
    @Test
    fun selectedApoiIsRepresentedBySharedBrowserState() {
        val apoi = apoi()
        val state = AppState(
            walking = walkingState(),
            apoiBrowser = ApoiBrowserState(
                query = ApoiBrowserQuery("route", 2.0),
                results = emptyList(),
                selected = apoi
            )
        )

        assertEquals("water", state.apoiBrowser?.selected?.id)
        assertEquals(2.0, state.walking?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test
    fun clearingSelectionKeepsWalkingContextUntouched() {
        val walking = walkingState()
        val state = AppState(
            walking = walking,
            apoiBrowser = ApoiBrowserState(
                query = ApoiBrowserQuery("route", 2.0),
                results = emptyList(),
                selected = apoi()
            )
        )

        val cleared = state.copy(
            apoiBrowser = state.apoiBrowser?.copy(selected = null)
        )

        assertEquals(walking, cleared.walking)
        assertEquals(null, cleared.apoiBrowser?.selected)
    }

    private fun walkingState() = WalkingState(
        walk = Walk("walk", "route", plannedStartKm = 1.0, plannedDestinationKm = 8.0),
        routePosition = RoutePosition("route", 2.0, 0.0),
        gpsState = GpsState.ON_ROUTE,
        progress = WalkingProgress(
            currentRouteKm = 2.0,
            walkedKm = 1.0,
            remainingKm = 6.0,
            targetRouteKm = 8.0,
            progressRatio = 1.0 / 7.0,
            stageId = null
        ),
        nextApoi = apoi(),
        nextApoiDistanceKm = 1.0,
        isOffline = false
    )

    private fun apoi() = Apoi(
        id = "water",
        name = "Fonte",
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", 3.0, 0.0, 0.0, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null),
        cost = ApoiCost(),
        reservation = ApoiReservation(),
        characteristics = ApoiCharacteristics(),
        contact = ApoiContact(),
        confidence = ApoiConfidence()
    )
}
