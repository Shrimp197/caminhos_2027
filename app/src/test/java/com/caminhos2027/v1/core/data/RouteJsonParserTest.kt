package com.caminhos2027.v1.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteJsonParserTest {
    @Test
    fun parsesRouteAndStagesFromV1Contract() {
        val route = RouteJsonParser.parse(
            """
            {
              "id":"test-route",
              "name":"TEST/FICTITIOUS route",
              "official_name":"TEST/FICTITIOUS route",
              "geometry":{},
              "total_distance_km":20.0,
              "stages":[{
                "id":"stage-1",
                "route_id":"test-route",
                "number":1,
                "name":"TEST/FICTITIOUS stage",
                "start_route_km":0.0,
                "end_route_km":10.0,
                "distance_km":10.0,
                "start_name":"TEST/FICTITIOUS start",
                "end_name":"TEST/FICTITIOUS end",
                "source":"TEST/FICTITIOUS"
              }],
              "source":"TEST/FICTITIOUS",
              "updated_at":"2026-09-02"
            }
            """.trimIndent()
        )

        assertEquals("test-route", route.id)
        assertEquals(20.0, route.totalDistanceKm, 0.0)
        assertEquals(1, route.stages.size)
        assertEquals(10.0, route.stages.first().distanceKm, 0.0)
    }
}
