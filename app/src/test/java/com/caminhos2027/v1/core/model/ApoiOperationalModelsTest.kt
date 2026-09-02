package com.caminhos2027.v1.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApoiOperationalModelsTest {
    @Test fun defaultsKeepCriticalInformationUnknown() {
        val cost = ApoiCost()
        val reservation = ApoiReservation()
        val availability = ApoiAvailability()
        val confidence = ApoiConfidence()

        assertEquals(ApoiCostModel.UNKNOWN, cost.model)
        assertEquals(ApoiReservationPolicy.UNKNOWN, reservation.policy)
        assertEquals(ApoiAvailabilityStatus.AWAITING_CONFIRMATION, availability.status)
        assertEquals(PositionConfidence.UNKNOWN, confidence.criticalInformation)
        assertNull(cost.amount)
    }

    @Test fun explicitOperationalValuesRemainDistinct() {
        val apoi = Apoi(
            id = "sleep-1",
            name = "Teste",
            description = null,
            mainCategory = ApoiCategory.PERNOITA,
            services = setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES),
            location = ApoiLocation(null, null, LocationPrecision.UNKNOWN, null, null, null, "route", 10.0, null, null, RouteRelation.ON_ROUTE),
            publication = ApoiPublication(PublicationStatus.PUBLISHED_WITH_WARNING, "confirmation pending"),
            cost = ApoiCost(ApoiCostModel.OPTIONAL_CONTRIBUTION, 5.0, "EUR", "contribution sugerida"),
            reservation = ApoiReservation(ApoiReservationPolicy.REQUIRED),
            availability = ApoiAvailability(ApoiAvailabilityStatus.AWAITING_CONFIRMATION),
            capacity = ApoiCapacity(total = 12, sleeping = 10),
            characteristics = ApoiCharacteristics(sleepingType = SleepingType.BED, shower = true, wc = true),
            confidence = ApoiConfidence(overall = PositionConfidence.MEDIUM)
        )

        assertEquals(ApoiCostModel.OPTIONAL_CONTRIBUTION, apoi.cost.model)
        assertEquals(ApoiReservationPolicy.REQUIRED, apoi.reservation.policy)
        assertEquals(12, apoi.capacity.total)
        assertEquals(SleepingType.BED, apoi.characteristics.sleepingType)
        assertEquals(PositionConfidence.MEDIUM, apoi.confidence.overall)
    }
}
