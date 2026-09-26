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
        assertTrue(emitted.last().capturedAt.isAfter(emitted[1].capturedAt))
    }

    @Test
    fun recoveryFixGetsARealTimestampGapForStateMachineRecovery() {
        val emitted = mutableListOf<RawGpsPosition>()
        var now = Instant.parse("2026-09-06T08:00:00Z")
        val source = GpxSimulationLocationSource(
            points = listOf(GeoPoint(41.1000, -8.5800)),
            onPosition = emitted::add,
            clock = { now }
        )

        source.start()
        source.setAvailable(false)
        now = emitted.last().capturedAt
        source.setAvailable(true)
        source.emitRecoveryFix()

        assertEquals(3, emitted.size)
        assertEquals(1_000L, java.time.Duration.between(emitted[1].capturedAt, emitted[2].capturedAt).toMillis())
    }

    @Test
    fun deviationEmitsRawPositionPerpendicularToTheSelectedRoutePoint() {
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
        source.simulateDeviation(offsetMeters = 55.0)

        assertEquals(2, emitted.size)
        assertEquals(points[0].latitude, emitted[1].latitude, 0.00005)
        assertTrue(kotlin.math.abs(emitted[1].longitude - emitted[0].longitude) > 0.0003)
        assertTrue(kotlin.math.abs(emitted[1].longitude - emitted[0].longitude) < 0.001)
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
