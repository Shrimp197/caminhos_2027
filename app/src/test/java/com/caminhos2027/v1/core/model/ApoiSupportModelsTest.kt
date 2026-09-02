package com.caminhos2027.v1.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApoiSupportModelsTest {
    @Test
    fun unknown_values_are_not_converted_to_positive_values() {
        val cost = ApoiCost()
        val reservation = ApoiReservation()
        val availability = ApoiAvailability()

        assertEquals(ApoiCostModel.UNKNOWN, cost.model)
        assertEquals(ApoiReservationPolicy.UNKNOWN, reservation.policy)
        assertEquals(ApoiAvailabilityStatus.AWAITING_CONFIRMATION, availability.status)
        assertNull(cost.amount)
    }

    @Test
    fun operational_fields_are_optional_for_existing_apoi() {
        val location = ApoiLocation(null, null, LocationPrecision.UNKNOWN, null, null, null, "route", 10.0, null, null, RouteRelation.ON_ROUTE)
        val apoi = Apoi("a", "Teste", null, ApoiCategory.DESCANSO, setOf(ApoiCategory.DESCANSO), location, ApoiPublication(PublicationStatus.PUBLISHED, null))

        assertEquals(ApoiCostModel.UNKNOWN, apoi.cost.model)
        assertEquals(ApoiReservationPolicy.UNKNOWN, apoi.reservation.policy)
        assertEquals(ApoiAvailabilityStatus.AWAITING_CONFIRMATION, apoi.availability.status)
    }
}
