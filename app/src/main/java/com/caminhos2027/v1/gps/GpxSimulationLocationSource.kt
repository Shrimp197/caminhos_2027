package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import java.time.Instant

/**
 * QA-only raw-position source backed by a selected GPX route.
 * It emits raw positions only; route projection, validation and walking state remain shared
 * with real GPS through the same callback path in the activity.
 *
 * Availability and deliberate deviation controls exist only to make the real GPS state machine
 * exercisable without requiring a physical signal failure during every QA run.
 */
class GpxSimulationLocationSource(
    points: List<GeoPoint>,
    private val onPosition: (RawGpsPosition) -> Unit,
    private val onAvailabilityChanged: (Boolean) -> Unit = {},
    private val clock: () -> Instant = Instant::now,
    private val initialIndex: Int = 0
) : LocationSource {
    private val points = points.toList()
    private var index = initialIndex.coerceIn(0, (points.size - 1).coerceAtLeast(0))
    private var started = false
    private var available = false
    private var lastCapturedAt: Instant? = null

    init {
        require(initialIndex >= 0) { "Initial index must be non-negative" }
    }

    override fun start() {
        if (started || points.isEmpty()) return
        started = true
        available = true
        onAvailabilityChanged(true)
        emitCurrentPoint()
    }

    override fun stop() {
        started = false
        available = false
    }

    fun advance() {
        if (!started || !available || index >= points.lastIndex) return
        var movedMeters = 0.0
        while (index < points.lastIndex && movedMeters < MIN_ADVANCE_METERS) {
            val previous = points[index]
            index += 1
            movedMeters += distanceMeters(previous, points[index])
        }
        val minimumAdvanceMillis = ((movedMeters / 1000.0) / MAX_SIMULATED_SPEED_KMH * 3_600_000.0)
            .toLong()
            .coerceAtLeast(MIN_ADVANCE_MILLIS)
        emitCurrentPoint(minimumAdvanceMillis = minimumAdvanceMillis)
    }

    /** Simulates a temporary loss of GPS without changing the last emitted position. */
    fun setAvailable(value: Boolean) {
        if (!started || available == value) return
        available = value
        onAvailabilityChanged(value)
        if (value) emitCurrentPoint()
    }

    /** Forces one fresh raw fix after a QA recovery command without bypassing the real pipeline. */
    fun emitRecoveryFix() {
        if (!started || !available || points.isEmpty()) return
        emitCurrentPoint(minimumAdvanceMillis = 1_000L)
    }

    /** Emits one deliberate off-route raw position for deterministic deviation QA. */
    fun simulateDeviation(offsetMeters: Double = 50.0) {
        require(offsetMeters.isFinite() && offsetMeters > 0.0) {
            "Deviation offset must be finite and > 0"
        }
        if (!started || !available || points.isEmpty()) return
        val point = points[index]
        val before = points.getOrNull((index - 1).coerceAtLeast(0)) ?: point
        val after = points.getOrNull((index + 1).coerceAtMost(points.lastIndex)) ?: point
        val latitudeMeters = 111_320.0
        val longitudeMeters = latitudeMeters * kotlin.math.cos(Math.toRadians(point.latitude)).coerceAtLeast(0.1)
        val north = (after.latitude - before.latitude) * latitudeMeters
        val east = (after.longitude - before.longitude) * longitudeMeters
        val length = kotlin.math.hypot(north, east).takeIf { it.isFinite() && it > 0.0 } ?: 1.0
        val offsetNorth = -east / length * offsetMeters
        val offsetEast = north / length * offsetMeters
        onPosition(
            RawGpsPosition(
                latitude = point.latitude + offsetNorth / latitudeMeters,
                longitude = point.longitude + offsetEast / longitudeMeters,
                accuracyMeters = 1.0,
                capturedAt = nextCapturedAt(minimumAdvanceMillis = 1_000L)
            )
        )
    }

    private fun emitCurrentPoint(minimumAdvanceMillis: Long = 0L) {
        val point = points[index]
        onPosition(
            RawGpsPosition(
                latitude = point.latitude,
                longitude = point.longitude,
                accuracyMeters = 1.0,
                capturedAt = nextCapturedAt(minimumAdvanceMillis)
            )
        )
    }

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val earthRadiusMeters = 6_371_008.8
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(b.longitude - a.longitude)
        val sinLat = kotlin.math.sin(deltaLat / 2.0)
        val sinLon = kotlin.math.sin(deltaLon / 2.0)
        val h = (sinLat * sinLat + kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * sinLon * sinLon)
            .coerceIn(0.0, 1.0)
        return 2.0 * earthRadiusMeters * kotlin.math.atan2(kotlin.math.sqrt(h), kotlin.math.sqrt(1.0 - h))
    }

    private fun nextCapturedAt(minimumAdvanceMillis: Long = 0L): Instant {
        require(minimumAdvanceMillis >= 0L)
        val now = clock()
        val previous = lastCapturedAt
        val next = if (previous == null) {
            now
        } else {
            val minimumNext = previous.plusMillis(minimumAdvanceMillis)
            if (now.isAfter(minimumNext)) now else minimumNext
        }
        lastCapturedAt = next
        return next
    }

    private companion object {
        const val MIN_ADVANCE_METERS = 10.0
        const val MAX_SIMULATED_SPEED_KMH = 8.0
        const val MIN_ADVANCE_MILLIS = 100L
    }
}
