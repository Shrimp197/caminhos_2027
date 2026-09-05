package com.caminhos2027.v1.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpxRouteParserTest {
    @Test
    fun parsesTrackPointsAndCalculatesPositiveDistance() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" xmlns="http://www.topografix.com/GPX/1/1">
              <trk><name>Teste</name><trkseg>
                <trkpt lat="41.1000" lon="-8.5800"/>
                <trkpt lat="41.1010" lon="-8.5800"/>
                <trkpt lat="41.1010" lon="-8.5810"/>
              </trkseg></trk>
            </gpx>
        """.trimIndent()

        val route = GpxRouteParser.parse(xml, "hf-test", "HF — trajeto de teste", "GPX QA HF")

        assertEquals("hf-test", route.id)
        assertEquals(3, route.geometry.points.size)
        assertTrue(route.totalDistanceKm > 0.1)
        assertEquals("GPX QA HF", route.source)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTrackWithoutEnoughPoints() {
        val xml = "<gpx><trk><trkseg><trkpt lat=\"41.1\" lon=\"-8.5\"/></trkseg></trk></gpx>"
        GpxRouteParser.parse(xml, "sr-test", "SR", "GPX QA SR")
    }
}
