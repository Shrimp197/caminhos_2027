package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpxSimulationLocationSourceTest {
    @Test
    fun emitsFirstPointOnStartAndAdvancesWithoutChangingTheRawPositionContract() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800),
            GeoPoint(41.1020, -8.5800)
        )
        val emitted = mutableListOf<com.caminhos2027.v1.core.model.RawGpsPosition>()
        var now = Instant.parse("2026-09-05T14:00:00Z")
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add,
            clock = { now }
        )

        source.start()
        now = now.plusSeconds(2)
        source.advance()
        now = now.plusSeconds(2)
        source.advance()
        source.advance()

        assertEquals(3, emitted.size)
        assertEquals(points.first().latitude, emitted[0].latitude)
        assertEquals(points.first().longitude, emitted[0].longitude)
        assertEquals(points[1].latitude, emitted[1].latitude)
        assertEquals(points[2].longitude, emitted[2].longitude)
        assertTrue(emitted.last().capturedAt.isAfter(emitted[1].capturedAt))
    }

    @Test
    fun startsAtTheRequestedInitialIndex() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800),
            GeoPoint(41.1020, -8.5800)
        )
        val emitted = mutableListOf<com.caminhos2027.v1.core.model.RawGpsPosition>()
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add,
            initialIndex = 1
        )

        source.start()
        source.advance()

        assertEquals(2, emitted.size)
        assertEquals(points[1].latitude, emitted[0].latitude)
        assertEquals(points[2].latitude, emitted[1].latitude)
    }

    @Test
    fun clampsAnInitialIndexPastTheLastPointToTheLastPoint() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800)
        )
        val emitted = mutableListOf<com.caminhos2027.v1.core.model.RawGpsPosition>()
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add,
            initialIndex = 99
        )

        source.start()

        assertEquals(1, emitted.size)
        assertEquals(points.last().latitude, emitted.single().latitude)
    }

    @Test
    fun supportsAVisibleAdvanceDistanceForWalkingUi() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800),
            GeoPoint(41.1020, -8.5800)
        )
        val emitted = mutableListOf<com.caminhos2027.v1.core.model.RawGpsPosition>()
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add
        )

        source.start()
        source.advance(minimumMeters = 150.0)

        assertEquals(points[2].latitude, emitted.last().latitude)
        assertTrue(distanceMeters(emitted.first(), emitted.last()) >= 150.0)
    }

    @Test
    fun keepsSimulationTimeAlignedWithEvaluatorClockWhenAdvancing() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800),
            GeoPoint(41.1020, -8.5800),
            GeoPoint(41.1030, -8.5800)
        )
        val emitted = mutableListOf<com.caminhos2027.v1.core.model.RawGpsPosition>()
        var now = Instant.parse("2026-09-05T14:00:00Z")
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add,
            clock = { now },
            onClockAdvance = { now = now.plusMillis(it) }
        )

        source.start()
        source.advance(150.0)

        assertEquals(emitted.last().capturedAt, now)
        assertTrue(emitted.last().capturedAt.isAfter(emitted.first().capturedAt))
    }

    @Test
    fun stopPreventsFurtherSimulation() {
        val emitted = mutableListOf<com.caminhos2027.v1.core.model.RawGpsPosition>()
        val source = GpxSimulationLocationSource(
            points = listOf(GeoPoint(41.1, -8.5), GeoPoint(41.2, -8.5)),
            onPosition = emitted::add
        )

        source.start()
        source.stop()
        source.advance()

        assertEquals(1, emitted.size)
    }
    private fun distanceMeters(a: com.caminhos2027.v1.core.model.RawGpsPosition, b: com.caminhos2027.v1.core.model.RawGpsPosition): Double {
        val earthRadiusMeters = 6_371_008.8
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(b.longitude - a.longitude)
        val sinLat = kotlin.math.sin(deltaLat / 2.0)
        val sinLon = kotlin.math.sin(deltaLon / 2.0)
        val h = (sinLat * sinLat + kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * sinLon * sinLon).coerceIn(0.0, 1.0)
        return 2.0 * earthRadiusMeters * kotlin.math.atan2(kotlin.math.sqrt(h), kotlin.math.sqrt(1.0 - h))
    }

}
