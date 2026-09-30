package com.caminhos2027.v1.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.walking.WalkingGuidanceTargetPolicy
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.sin

internal fun pointAtRouteKmForNavigation(route: Route, routeKm: Double): GeoPoint =
    WalkingGuidanceTargetPolicy.pointAtRouteKm(route, routeKm)

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

