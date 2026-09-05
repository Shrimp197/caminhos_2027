package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import java.time.Instant

/**
 * QA-only raw-position source backed by a selected GPX route.
 * It emits raw positions only; route projection, validation and walking state remain shared
 * with real GPS through the same callback path in the activity.
 */
class GpxSimulationLocationSource(
    points: List<GeoPoint>,
    private val onPosition: (RawGpsPosition) -> Unit,
    private val onAvailabilityChanged: (Boolean) -> Unit = {},
    private val clock: () -> Instant = Instant::now
) : LocationSource {
    private val points = points.toList()
    private var index = 0
    private var started = false

    override fun start() {
        if (started || points.isEmpty()) return
        started = true
        onAvailabilityChanged(true)
        emitCurrentPoint()
    }

    override fun stop() {
        started = false
    }

    fun advance() {
        if (!started || index >= points.lastIndex) return
        index += 1
        emitCurrentPoint()
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
