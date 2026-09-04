package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Projects a physical GPS position onto the nearest segment of the ordered route.
 * This engine is deterministic and independent of Android location APIs.
 */
object RouteLocationEngine {
    private const val EARTH_RADIUS_M = 6_371_008.8
    private const val HIGH_CONFIDENCE_ACCURACY_M = 20.0

    fun locate(route: Route, gps: RawGpsPosition): RoutePosition {
        require(route.geometry.points.size >= 2) { "Route geometry must contain at least two points" }
        require(gps.latitude.isFinite() && gps.longitude.isFinite()) { "GPS coordinates must be finite" }
        require(gps.latitude in -90.0..90.0) { "GPS latitude must be within Earth bounds" }
        require(gps.longitude in -180.0..180.0) { "GPS longitude must be within Earth bounds" }

        var bestDistance = Double.POSITIVE_INFINITY
        var bestRouteKm = 0.0
        var accumulatedKm = 0.0

        route.geometry.points.zipWithNext().forEach { (start, end) ->
            val segmentLengthKm = distanceKm(start, end)
            val projection = project(gps.latitude, gps.longitude, start, end)
            if (projection.distanceMeters < bestDistance) {
                bestDistance = projection.distanceMeters
                bestRouteKm = accumulatedKm + segmentLengthKm * projection.fraction
            }
            accumulatedKm += segmentLengthKm
        }

        require(bestDistance.isFinite() && bestRouteKm.isFinite()) {
            "Route projection produced non-finite metrics"
        }

        val stageId = StageLocator.currentStage(route, bestRouteKm)?.id
        val confidence = confidenceFor(
            distanceToRouteMeters = bestDistance,
            accuracyMeters = gps.accuracyMeters
        )

        return RoutePosition(
            routeId = route.id,
            routeKm = bestRouteKm,
            distanceToRouteMeters = bestDistance,
            stageId = stageId,
            confidence = confidence
        )
    }

    private fun confidenceFor(distanceToRouteMeters: Double, accuracyMeters: Double?): PositionConfidence {
        if (accuracyMeters == null || !accuracyMeters.isFinite() || accuracyMeters < 0.0) {
            return PositionConfidence.UNKNOWN
        }
        return when {
            accuracyMeters <= HIGH_CONFIDENCE_ACCURACY_M && distanceToRouteMeters <= 35.0 -> PositionConfidence.HIGH
            accuracyMeters <= 50.0 && distanceToRouteMeters <= 80.0 -> PositionConfidence.MEDIUM
            else -> PositionConfidence.LOW
        }
    }

    private data class Projection(val fraction: Double, val distanceMeters: Double)

    private fun project(
        latitude: Double,
        longitude: Double,
        start: GeoPoint,
        end: GeoPoint
    ): Projection {
        require(start.latitude.isFinite() && start.longitude.isFinite() && end.latitude.isFinite() && end.longitude.isFinite()) {
            "Route geometry coordinates must be finite"
        }
        require(start.latitude in -90.0..90.0 && end.latitude in -90.0..90.0) {
            "Route geometry latitude must be within Earth bounds"
        }
        require(start.longitude in -180.0..180.0 && end.longitude in -180.0..180.0) {
            "Route geometry longitude must be within Earth bounds"
        }

        val meanLat = Math.toRadians((start.latitude + end.latitude + latitude) / 3.0)
        val metersPerDegreeLat = 111_320.0
        val metersPerDegreeLon = metersPerDegreeLat * cos(meanLat)

        fun x(lon: Double) = lon * metersPerDegreeLon
        fun y(lat: Double) = lat * metersPerDegreeLat

        val sx = x(start.longitude)
        val sy = y(start.latitude)
        val ex = x(end.longitude)
        val ey = y(end.latitude)
        val px = x(longitude)
        val py = y(latitude)

        val dx = ex - sx
        val dy = ey - sy
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared <= 0.0) {
            return Projection(0.0, hypot(px - sx, py - sy))
        }

        val rawFraction = ((px - sx) * dx + (py - sy) * dy) / lengthSquared
        val fraction = min(1.0, max(0.0, rawFraction))
        val nearestX = sx + fraction * dx
        val nearestY = sy + fraction * dy
        return Projection(fraction, hypot(px - nearestX, py - nearestY))
    }

    private fun distanceKm(a: GeoPoint, b: GeoPoint): Double =
        distanceMeters(a.latitude, a.longitude, b.latitude, b.longitude) / 1000.0

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dp = p2 - p1
        val dl = Math.toRadians(lon2 - lon1)
        val sinLat = kotlin.math.sin(dp / 2.0)
        val sinLon = kotlin.math.sin(dl / 2.0)
        val h = (sinLat * sinLat + kotlin.math.cos(p1) * kotlin.math.cos(p2) * sinLon * sinLon).coerceIn(0.0, 1.0)
        return 2.0 * EARTH_RADIUS_M * kotlin.math.atan2(sqrt(h), sqrt(1.0 - h))
    }

    private fun hypot(x: Double, y: Double): Double = sqrt(x * x + y * y)
}
