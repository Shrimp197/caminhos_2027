package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RouteGeometry
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Deterministic measurements used to validate imported route geometry. */
object RouteGeometryMetrics {
    private const val EARTH_RADIUS_KM = 6371.0088

    fun lengthKm(geometry: RouteGeometry): Double =
        geometry.points.zipWithNext().sumOf { (a, b) -> distanceKm(a, b) }

    private fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val sinLat = sin(dLat / 2.0)
        val sinLon = sin(dLon / 2.0)
        val h = sinLat.pow(2) + cos(lat1) * cos(lat2) * sinLon.pow(2)
        return 2.0 * EARTH_RADIUS_KM * atan2(sqrt(h), sqrt(1.0 - h))
    }
}
