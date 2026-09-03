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

    @Test
    fun parseRejectsMultipleTracks() {
        assertThrows(IllegalArgumentException::class.java) {
            GpxRouteImporter.parse(gpx(tracks = 2))
        }
    }

    @Test
    fun parseRejectsMultipleSegments() {
        assertThrows(IllegalArgumentException::class.java) {
            GpxRouteImporter.parse(gpx(segments = 2))
        }
    }

    private fun gpx(
        lat1: String? = "41.1",
        lon1: String? = "-8.6",
        lat2: String? = "41.2",
        lon2: String? = "-8.5",
        tracks: Int = 1,
        segments: Int = 1,
    ) = ByteArrayInputStream(
        buildString {
            appendLine("<?xml version=\"1.0\"?>")
            appendLine("<gpx version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\">")
            repeat(tracks) { trackIndex ->
                appendLine("  <trk>")
                repeat(segments) { segmentIndex ->
                    appendLine("    <trkseg>")
                    if (trackIndex == 0 && segmentIndex == 0) {
                        if (lat1 != null) appendLine("      <trkpt lat=\"$lat1\" lon=\"$lon1\"/>")
                        else appendLine("      <trkpt lon=\"$lon1\"/>")
                        appendLine("      <trkpt lat=\"$lat2\" lon=\"$lon2\"/>")
                    } else {
                        appendLine("      <trkpt lat=\"41.3\" lon=\"-8.4\"/>")
                    }
                    appendLine("    </trkseg>")
                }
                appendLine("  </trk>")
            }
            appendLine("</gpx>")
        }.toByteArray()
    )
}
