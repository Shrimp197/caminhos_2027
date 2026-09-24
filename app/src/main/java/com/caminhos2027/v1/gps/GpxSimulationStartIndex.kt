package com.caminhos2027.v1.gps

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Finds the GPX point nearest to a planned route kilometre so QA playback starts
 * where the prepared walk says it starts. This is test infrastructure only.
 *
 * The route points are already ordered and the GPX parser computes route distance from
 * consecutive points. Reusing those cumulative distances keeps startup linear in the
 * number of GPX points instead of projecting every point against every route segment.
 */
object GpxSimulationStartIndex {
    fun nextPointIndexAfterKm(route: Route, currentRouteKm: Double): Int {
        require(route.geometry.points.size >= 2) { "Route geometry must contain at least two points" }
        require(currentRouteKm.isFinite()) { "Current route kilometre must be finite" }

        val target = currentRouteKm.coerceIn(0.0, route.totalDistanceKm)
        val cumulative = cumulativeRouteKm(route.geometry.points)
        return cumulative.indexOfFirst { it > target + 1e-9 }
            .takeIf { it >= 0 }
            ?: route.geometry.points.lastIndex
    }

    fun nearestPointIndex(route: Route, plannedStartKm: Double): Int {
        require(route.geometry.points.size >= 2) { "Route geometry must contain at least two points" }
        require(plannedStartKm.isFinite()) { "Planned start kilometre must be finite" }

        val target = plannedStartKm.coerceIn(0.0, route.totalDistanceKm)
        return cumulativeRouteKm(route.geometry.points)
            .indices
            .minBy { index -> abs(cumulativeRouteKm(route.geometry.points)[index] - target) }
    }

    private fun cumulativeRouteKm(points: List<GeoPoint>): List<Double> {
        val cumulative = ArrayList<Double>(points.size)
        var distanceKm = 0.0
        cumulative += distanceKm
        points.zipWithNext().forEach { (from, to) ->
            distanceKm += distanceKm(from, to)
            cumulative += distanceKm
        }
        return cumulative
    }

    private fun distanceKm(from: GeoPoint, to: GeoPoint): Double =
        distanceMeters(from.latitude, from.longitude, to.latitude, to.longitude) / 1000.0

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val deltaLat = p2 - p1
        val deltaLon = Math.toRadians(lon2 - lon1)
        val sinLat = sin(deltaLat / 2.0)
        val sinLon = sin(deltaLon / 2.0)
        val a = (sinLat * sinLat + cos(p1) * cos(p2) * sinLon * sinLon).coerceIn(0.0, 1.0)
        return 2.0 * 6_371_008.8 * atan2(sqrt(a), sqrt(1.0 - a))
    }
}
