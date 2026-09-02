package com.caminhos2027.v1.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApoiJsonParserTest {
    @Test
    fun parsesFullV1ApoiDatasetFields() {
        val json = javaClass.classLoader!!.getResource("apoi-v1-test.json")!!.readText()
        val apoi = ApoiJsonParser.parse(json).apoi.first()

        assertEquals("TEST-FICTITIOUS-APOI-001", apoi.id)
        assertEquals(3, apoi.services.size)
        assertEquals(10.0, apoi.location.routeKm!!, 0.0)
        assertEquals("RECURRING", apoi.availability.status.name)
        assertEquals(5.0, apoi.cost.amount!!, 0.0)
        assertEquals("OPTIONAL_CONTRIBUTION", apoi.cost.model.name)
        assertEquals("RECOMMENDED", apoi.reservation.policy.name)
        assertEquals(12, apoi.support.capacity)
        assertTrue(apoi.support.pilgrimSupportConfirmed == true)
        assertTrue(apoi.support.waterPotable == true)
        assertTrue(apoi.support.waterPotableConfirmed == true)
        assertEquals("MEDIUM", apoi.confidence.availability.name)
        assertEquals(1, apoi.sources.size)
        assertEquals("TEST-SOURCE-001", apoi.sources.first().id)
    }

    @Test
    fun optionalExtendedFieldsKeepSafeDefaultsWhenAbsent() {
        val json = """
            {
              "dataset_version":"TEST-FICTITIOUS-minimal",
              "environment":"sr",
              "apoi":[{
                "id":"TEST-FICTITIOUS-002",
                "name":"TEST/FICTITIOUS minimal",
                "main_category":"AGUA",
                "services":["AGUA"],
                "location":{"precision":"LOCALITY_ONLY","route_relation":"NEAR_ROUTE"},
                "publication":{"status":"PUBLISHED_WITH_WARNING","reason":"TEST/FICTITIOUS"}
              }]
            }
        """.trimIndent()

        val apoi = ApoiJsonParser.parse(json).apoi.first()

        assertEquals("UNKNOWN", apoi.cost.model.name)
        assertEquals("UNKNOWN", apoi.reservation.policy.name)
        assertEquals("AWAITING_CONFIRMATION", apoi.availability.status.name)
        assertEquals("UNKNOWN", apoi.confidence.overall.name)
        assertTrue(apoi.sources.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidDatasetThroughDomainValidator() {
        ApoiJsonParser.parse(
            """
            {
              "dataset_version":"TEST-FICTITIOUS-invalid",
              "environment":"production",
              "apoi":[{
                "id":"TEST-FICTITIOUS-001",
                "name":"TEST/FICTITIOUS",
                "main_category":"PERNOITA",
                "services":["PERNOITA"],
                "location":{"precision":"UNKNOWN","route_relation":"ON_ROUTE"},
                "publication":{"status":"REVIEW"}
              }]
            }
            """.trimIndent()
        )
    }
}
