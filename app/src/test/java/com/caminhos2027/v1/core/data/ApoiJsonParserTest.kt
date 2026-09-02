package com.caminhos2027.v1.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiJsonParserTest {
    @Test
    fun parsesMultiServiceApoiDataset() {
        val json = javaClass.classLoader!!.getResource("apoi-v1-test.json")!!.readText()
        val dataset = ApoiJsonParser.parse(json)

        assertEquals("TEST-FICTITIOUS-v1", dataset.datasetVersion)
        assertEquals("sr", dataset.environment)
        assertEquals(1, dataset.apoi.size)
        assertEquals("TEST-FICTITIOUS-APOI-001", dataset.apoi.first().id)
        assertEquals(3, dataset.apoi.first().services.size)
        assertEquals(10.0, dataset.apoi.first().location.routeKm!!, 0.0)
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
