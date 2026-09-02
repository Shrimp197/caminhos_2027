package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
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

    fun locate(route: Route, gps: RawGpsPosition): RoutePosition {
        require(route.geometry.points.size >= 2) { "Route geometry must contain at least two points" }

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

        val stageId = StageLocator.currentStage(route, bestRouteKm)?.id

        return RoutePosition(
            routeId = route.id,
            routeKm = bestRouteKm,
            distanceToRouteMeters = bestDistance,
            stageId = stageId
        )
    }

    private data class Projection(val fraction: Double, val distanceMeters: Double)

    private fun project(
        latitude: Double,
        longitude: Double,
        start: GeoPoint,
        end: GeoPoint
    ): Projection {
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
        val h = sinLat * sinLat + kotlin.math.cos(p1) * kotlin.math.cos(p2) * sinLon * sinLon
        return 2.0 * EARTH_RADIUS_M * kotlin.math.atan2(sqrt(h), sqrt(1.0 - h))
    }

    private fun hypot(x: Double, y: Double): Double = sqrt(x * x + y * y)
}
