package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiCostModel
import com.caminhos2027.v1.core.model.ApoiReservationPolicy
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.SleepingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApoiJsonParserTest {
    @Test
    fun `parses complete APOI contract including optional fields`() {
        val json = """
            {
              "items": [
                {
                  "id": "test-001",
                  "name": "Apoio Teste",
                  "description": "pernoita, duche",
                  "main_category": "PERNOITA",
                  "services": ["PERNOITA", "DUCHES"],
                  "location": {
                    "latitude": 40.5,
                    "longitude": -8.5,
                    "precision": "EXACT",
                    "locality": "Teste",
                    "municipality": "Município Teste",
                    "reference": "junto à igreja",
                    "route_id": "caminho-centenario",
                    "route_km": 42.25,
                    "distance_to_route_m": 18.0,
                    "access_distance_m": 30.0,
                    "route_relation": "ON_ROUTE"
                  },
                  "cost": {"model": "PAID", "amount": 10.0, "currency": "EUR", "description": "2026"},
                  "reservation": {"policy": "REQUIRED", "contact": "123", "url": "https://example.invalid", "notes": "confirmar"},
                  "availability": {"status": "AWAITING_CONFIRMATION", "valid_from": "2027-01-01", "valid_until": null, "recurrence": "annual", "season": null, "opening_hours": "09:00-18:00", "notes": "confirmar 2027"},
                  "capacity": {"total": 12, "sleeping": 10, "notes": null},
                  "characteristics": {"sleeping_type": "BED", "shower": true, "hot_water": true, "wc": true, "laundry": false, "drying": true, "accessibility": null, "notes": null},
                  "contact": {"responsible": null, "organization": "Apoio Teste", "phone": "123", "email": null, "website": null, "social": null},
                  "confidence": {"overall": "MEDIUM", "location": "HIGH", "support": "MEDIUM", "availability": "LOW", "critical_information": "LOW"},
                  "publication": {"status": "REVIEW", "reason": "confirmation pending"}
                }
              ]
            }
        """.trimIndent()

        val record = ApoiJsonParser.parseDataset(json).single()

        assertEquals("test-001", record.id)
        assertEquals(ApoiCategory.PERNOITA, record.mainCategory)
        assertEquals(setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES), record.services)
        assertEquals(LocationPrecision.EXACT, record.location.precision)
        assertEquals(RouteRelation.ON_ROUTE, record.location.routeRelation)
        assertEquals(42.25, record.location.routeKm, 0.0001)
        assertEquals(ApoiCostModel.PAID, record.cost.model)
        assertEquals(10.0, record.cost.amount!!, 0.0001)
        assertEquals(ApoiReservationPolicy.REQUIRED, record.reservation.policy)
        assertEquals(ApoiAvailabilityStatus.AWAITING_CONFIRMATION, record.availability.status)
        assertEquals(SleepingType.BED, record.characteristics.sleepingType)
        assertTrue(record.characteristics.shower == true)
        assertEquals(PublicationStatus.REVIEW, record.publication.status)
    }

    @Test
    fun `missing nullable fields stay null and missing optional sections use safe defaults`() {
        val json = """
            {
              "items": [
                {
                  "id": "test-002",
                  "name": "Apoio Mínimo",
                  "main_category": "AGUA",
                  "services": ["AGUA"],
                  "location": {
                    "precision": "LOCALITY_ONLY",
                    "route_relation": "LOCATION_UNCERTAIN"
                  }
                }
              ]
            }
        """.trimIndent()

        val record = ApoiJsonParser.parseDataset(json).single()

        assertNull(record.location.latitude)
        assertNull(record.location.routeKm)
        assertEquals(LocationPrecision.LOCALITY_ONLY, record.location.precision)
        assertEquals(RouteRelation.LOCATION_UNCERTAIN, record.location.routeRelation)
        assertEquals(ApoiCostModel.UNKNOWN, record.cost.model)
        assertEquals(ApoiReservationPolicy.UNKNOWN, record.reservation.policy)
        assertEquals(ApoiAvailabilityStatus.AWAITING_CONFIRMATION, record.availability.status)
        assertEquals(PublicationStatus.REVIEW, record.publication.status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown enum value fails fast`() {
        ApoiJsonParser.parseDataset(
            """
              {"items":[{"id":"test-003","name":"Inválido","main_category":"INVALID","services":["AGUA"],"location":{"precision":"EXACT","route_relation":"ON_ROUTE"}}]}
            """.trimIndent()
        )
    }
}
