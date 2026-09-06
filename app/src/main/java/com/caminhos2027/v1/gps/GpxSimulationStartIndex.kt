package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.RouteLocationEngine
import java.time.Instant
import kotlin.math.abs

/**
 * Finds the GPX point nearest to a planned route kilometre so QA playback starts
 * where the prepared walk says it starts. This is test infrastructure only.
 */
object GpxSimulationStartIndex {
    fun nearestPointIndex(route: Route, plannedStartKm: Double): Int {
        require(route.geometry.points.isNotEmpty()) { "Route geometry must contain at least one point" }
        require(plannedStartKm.isFinite()) { "Planned start kilometre must be finite" }

        val target = plannedStartKm.coerceIn(0.0, route.totalDistanceKm)
        val capturedAt = Instant.EPOCH
        return route.geometry.points.indices.minBy { index ->
            val point = route.geometry.points[index]
            val projection = RouteLocationEngine.locate(
                route,
                RawGpsPosition(
                    latitude = point.latitude,
                    longitude = point.longitude,
                    accuracyMeters = 1.0,
                    capturedAt = capturedAt
                )
            )
            abs(projection.routeKm - target)
        }
    }
}
