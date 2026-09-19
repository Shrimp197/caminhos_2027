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
        index += 1
        emitCurrentPoint()
    }

    /** Simulates a temporary loss of GPS without changing the last emitted position. */
    fun setAvailable(value: Boolean) {
        if (!started || available == value) return
        available = value
        onAvailabilityChanged(value)
        if (value) emitCurrentPoint()
    }

    /** Emits one deliberate off-route raw position for deterministic deviation QA. */
    fun simulateDeviation(offsetDegrees: Double = 0.01) {
        require(offsetDegrees.isFinite() && offsetDegrees > 0.0) {
            "Deviation offset must be finite and > 0"
        }
        if (!started || !available || points.isEmpty()) return
        val point = points[index]
        onPosition(
            RawGpsPosition(
                latitude = point.latitude + offsetDegrees,
                longitude = point.longitude + offsetDegrees,
                accuracyMeters = 1.0,
                capturedAt = clock()
            )
        )
    }

    private fun emitCurrentPoint() {
        val point = points[index]
        onPosition(
            RawGpsPosition(
                latitude = point.latitude,
                longitude = point.longitude,
                accuracyMeters = 1.0,
                capturedAt = clock()
            )
        )
    }
}
