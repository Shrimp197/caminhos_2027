package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route

/** Chooses the physical point a pilgrim should be guided back to when starting/rejoining a walk. */
object WalkingGuidanceTargetPolicy {
    data class Target(val point: GeoPoint, val label: String)

    fun forPendingStart(
        currentDistanceToRouteMeters: Double,
        possibleDeviationMeters: Double,
        plannedStart: GeoPoint,
        routeName: String
    ): Target? =
        if (currentDistanceToRouteMeters >= possibleDeviationMeters) {
            Target(plannedStart, "Ir para o início da caminhada · $routeName")
        } else {
            null
        }

    fun forActiveWalk(
        currentDistanceToRouteMeters: Double,
        possibleDeviationMeters: Double,
        lastKnownOnRoute: GeoPoint?
    ): Target? =
        if (currentDistanceToRouteMeters >= possibleDeviationMeters && lastKnownOnRoute != null) {
            Target(lastKnownOnRoute, "Regressar ao último ponto conhecido no Caminho")
        } else {
            null
        }

    fun pointAtRouteKm(route: Route, routeKm: Double): GeoPoint {
        val points = route.geometry.points
        require(points.isNotEmpty()) { "Route geometry must contain at least one point" }
        if (points.size == 1) return points.first()
        val targetRouteKm = routeKm.coerceIn(0.0, route.totalDistanceKm)
        val geometryTotalKm = points.zipWithNext().sumOf { (a, b) -> distanceKm(a, b) }
        if (geometryTotalKm <= 0.0 || route.totalDistanceKm <= 0.0) return points.first()
        val targetGeometryKm = targetRouteKm / route.totalDistanceKm * geometryTotalKm
        var accumulatedKm = 0.0
        for (index in 1 until points.size) {
            val a = points[index - 1]
            val b = points[index]
            val segment = distanceKm(a, b)
            if (accumulatedKm + segment >= targetGeometryKm) {
                val fraction = if (segment <= 0.0) 0.0 else ((targetGeometryKm - accumulatedKm) / segment).coerceIn(0.0, 1.0)
                return GeoPoint(
                    latitude = a.latitude + (b.latitude - a.latitude) * fraction,
                    longitude = a.longitude + (b.longitude - a.longitude) * fraction
                )
            }
            accumulatedKm += segment
        }
        return points.last()
    }

    private fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
        val earthRadiusKm = 6371.0088
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val sinLat = kotlin.math.sin(dLat / 2.0)
        val sinLon = kotlin.math.sin(dLon / 2.0)
        val h = sinLat * sinLat + kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * sinLon * sinLon
        return 2.0 * earthRadiusKm * kotlin.math.asin(kotlin.math.sqrt(h.coerceIn(0.0, 1.0)))
    }
}
