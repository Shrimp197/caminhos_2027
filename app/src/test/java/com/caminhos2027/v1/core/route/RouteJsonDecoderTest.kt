package com.caminhos2027.v1.core.route

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteJsonDecoderTest {
    @Test
    fun decodesRouteAndStages() {
        val route = RouteJsonDecoder.decode(validJson())

        assertEquals("sr-fixture", route.id)
        assertEquals(1.0, route.totalDistanceKm, 0.0001)
        assertEquals(3, route.geometry.points.size)
        assertEquals(1, route.stages.size)
        assertEquals("stage-1", route.stages.first().id)
        assertEquals(1.0, route.stages.first().distanceKm, 0.0001)
    }

    @Test
    fun decodedRoutePassesValidation() {
        val route = RouteJsonDecoder.decode(validJson())
        val result = RouteDatasetValidator.validate(route)

        assertTrue(result.valid)
    }

    private fun validJson() = """
        {
          "id": "sr-fixture",
          "name": "SR — Percurso de teste",
          "officialName": "SR — Percurso de teste",
          "totalDistanceKm": 1.0,
          "source": "test-fixture",
          "updatedAt": "2026-09-02",
          "geometry": {
            "points": [
              {"latitude": 40.0000, "longitude": -8.0000},
              {"latitude": 40.0045, "longitude": -8.0000},
              {"latitude": 40.0090, "longitude": -8.0000}
            ]
          },
          "stages": [
            {
              "id": "stage-1",
              "routeId": "sr-fixture",
              "number": 1,
              "name": "Etapa de teste",
              "startRouteKm": 0.0,
              "endRouteKm": 1.0,
              "distanceKm": 1.0,
              "startName": "Início teste",
              "endName": "Fim teste",
              "source": "test-fixture"
            }
          ]
        }
    """.trimIndent()
}
