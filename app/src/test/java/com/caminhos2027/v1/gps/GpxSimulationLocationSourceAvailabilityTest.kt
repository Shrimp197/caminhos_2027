package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GpxSimulationLocationSourceAvailabilityTest {
    @Test
    fun controlledSignalLossFreezesEmissionAndRecoveryResumesFromLastRoutePoint() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800),
            GeoPoint(41.1020, -8.5800)
        )
        val emitted = mutableListOf<RawGpsPosition>()
        val availability = mutableListOf<Boolean>()
        var now = Instant.parse("2026-09-06T08:00:00Z")
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add,
            onAvailabilityChanged = availability::add,
            clock = { now }
        )

        source.start()
        now = now.plusSeconds(5)
        source.advance()
        val countBeforeLoss = emitted.size

        source.setAvailable(false)
        now = now.plusSeconds(10)
        source.advance()
        assertEquals(countBeforeLoss, emitted.size)

        now = now.plusSeconds(3)
        source.setAvailable(true)

        assertEquals(listOf(true, false, true), availability)
        assertEquals(countBeforeLoss + 1, emitted.size)
        assertEquals(points[1].latitude, emitted.last().latitude)
        assertEquals(points[1].longitude, emitted.last().longitude)
        assertEquals(now, emitted.last().capturedAt)
    }

    @Test
    fun deviationEmitsRawPositionWithoutChangingTheSelectedRoutePoint() {
        val points = listOf(
            GeoPoint(41.1000, -8.5800),
            GeoPoint(41.1010, -8.5800)
        )
        val emitted = mutableListOf<RawGpsPosition>()
        val source = GpxSimulationLocationSource(
            points = points,
            onPosition = emitted::add
        )

        source.start()
        source.simulateDeviation(offsetDegrees = 0.01)

        assertEquals(2, emitted.size)
        assertTrue(emitted[1].latitude > emitted[0].latitude)
        assertTrue(emitted[1].longitude > emitted[0].longitude)
        assertEquals(points[0].latitude + 0.01, emitted[1].latitude)
        assertEquals(points[0].longitude + 0.01, emitted[1].longitude)
    }

    @Test
    fun invalidDeviationOffsetIsRejected() {
        val source = GpxSimulationLocationSource(
            points = listOf(GeoPoint(41.1000, -8.5800), GeoPoint(41.1010, -8.5800)),
            onPosition = {}
        )

        kotlin.test.assertFailsWith<IllegalArgumentException> {
            source.simulateDeviation(0.0)
        }
    }
}
