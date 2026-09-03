package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream

class GpxRouteImporterTest {
    @Test
    fun parseReadsOrderedTrackPoints() {
        val geometry = GpxRouteImporter.parse(gpx("41.1", "-8.6", "41.2", "-8.5"))

        assertEquals(listOf(GeoPoint(41.1, -8.6), GeoPoint(41.2, -8.5)), geometry.points)
    }

    @Test
    fun parseRejectsMissingLatitude() {
        assertThrows(IllegalStateException::class.java) {
            GpxRouteImporter.parse(gpx(null, "-8.6", "41.2", "-8.5"))
        }
    }

    private fun gpx(lat1: String?, lon1: String?, lat2: String?, lon2: String?) = ByteArrayInputStream(
        """
        <?xml version="1.0"?>
        <gpx version="1.1" xmlns="http://www.topografix.com/GPX/1/1">
          <trk><trkseg>
            ${if (lat1 != null) "<trkpt lat=\"$lat1\" lon=\"$lon1\"/>" else "<trkpt lon=\"$lon1\"/>"}
            <trkpt lat="$lat2" lon="$lon2"/>
          </trkseg></trk>
        </gpx>
        """.trimIndent().toByteArray()
    )
}
