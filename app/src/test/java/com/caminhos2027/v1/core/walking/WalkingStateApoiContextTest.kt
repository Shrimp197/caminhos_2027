package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.model.Publication
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiServices
import com.caminhos2027.v1.core.model.ApoiSupport
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiCost
import com.caminhos2027.v1.core.model.ApoiReservation
import com.caminhos2027.v1.core.model.ApoiContact
import com.caminhos2027.v1.core.model.ApoiConfidence
import com.caminhos2027.v1.core.model.ApoiSources
import com.caminhos2027.v1.core.model.ApoiHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WalkingStateApoiContextTest {
    private val route = Route(
        "sr-route", "SR", "SR synthetic route", 2.0, "SR", "2026-09-01",
        RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))),
        listOf(Stage("stage-1", "sr-route", 1, "Stage 1", 0.0, 1.0, 1.0, "A", "B", "SR"), Stage("stage-2", "sr-route", 2, "Stage 2", 1.0, 2.0, 1.0, "B", "C", "SR"))
    )

    @Test fun nextApoiIsCalculatedByRouteDistance() {
        val walk = Walk("w", "sr-route", .0, 2.0, .0, null, null, null, WalkStatus.ACTIVE, listOf("stage-1", "stage-2"))
        val apoi = Apoi(
            id = "water-1", name = "Água SR", description = null, mainCategory = ApoiCategory.WATER,
            services = ApoiServices(), location = ApoiLocation(40.01, -8.0, routeId = "sr-route", routeKm = 1.2),
            support = ApoiSupport(), availability = ApoiAvailability(), cost = ApoiCost(), reservation = ApoiReservation(),
            contact = ApoiContact(), confidence = ApoiConfidence(), sources = ApoiSources(), history = ApoiHistory(),
            publication = Publication(PublicationStatus.PUBLISHED)
        )
        val state = WalkingStateBuilder.build(route, walk, com.caminhos2027.v1.core.route.GpsState.ON_ROUTE,
            RoutePosition("sr-route", .8, 2.0, "stage-1", PositionConfidence.HIGH), listOf(apoi))
        assertNotNull(state.nextApoi)
        assertEquals("water-1", state.nextApoi!!.id)
        assertEquals(.4, state.nextApoiDistanceKm!!, .001)
    }
}
