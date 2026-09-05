package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assertEquals(now.minusSeconds(2), emitted.last().capturedAt)
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
}
