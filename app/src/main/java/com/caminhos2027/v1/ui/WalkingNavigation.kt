package com.caminhos2027.v1.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.sin

internal fun pointAtRouteKmForNavigation(route: Route, routeKm: Double): GeoPoint {
    val points = route.geometry.points
    if (points.isEmpty()) return GeoPoint(0.0, 0.0)
    if (points.size == 1) return points.first()
    val target = routeKm.coerceIn(0.0, route.totalDistanceKm)
    var accumulated = 0.0
    for (index in 1 until points.size) {
        val a = points[index - 1]
        val b = points[index]
        val segment = geoDistanceKmForNavigation(a, b)
        if (accumulated + segment >= target) {
            val fraction = if (segment <= 0.0) 0.0 else ((target - accumulated) / segment).coerceIn(0.0, 1.0)
            return GeoPoint(
                latitude = a.latitude + (b.latitude - a.latitude) * fraction,
                longitude = a.longitude + (b.longitude - a.longitude) * fraction
            )
        }
        accumulated += segment
    }
    return points.last()
}

internal fun openWalkingNavigation(context: Context, target: GeoPoint, label: String) {
    val googleUri = Uri.parse("google.navigation:q=${target.latitude},${target.longitude}&mode=w")
    val googleIntent = Intent(Intent.ACTION_VIEW, googleUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(googleIntent)
        return
    } catch (_: ActivityNotFoundException) {
        // Fall through to any installed map application.
    }
    val geoIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:${target.latitude},${target.longitude}?q=${target.latitude},${target.longitude}(${Uri.encode(label)})")
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(geoIntent) }
}

private fun geoDistanceKmForNavigation(a: GeoPoint, b: GeoPoint): Double {
    val earthRadiusKm = 6371.0088
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val sinLat = sin(dLat / 2.0)
    val sinLon = sin(dLon / 2.0)
    val h = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
    return 2.0 * earthRadiusKm * asin(sqrt(h.coerceIn(0.0, 1.0)))
}
