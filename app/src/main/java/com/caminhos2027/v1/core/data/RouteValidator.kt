package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.RouteGeometryMetrics
import kotlin.math.abs

/** Deterministic domain validation for official route data before it reaches the app. */
object RouteValidator {
    private const val DISTANCE_TOLERANCE_KM = 0.1
    private const val GEOMETRY_DISTANCE_TOLERANCE_RATIO = 0.15

    fun validate(route: Route): List<String> {
        val errors = mutableListOf<String>()

        if (route.id.isBlank()) errors += "route.id must not be blank"
        if (route.name.isBlank()) errors += "route.name must not be blank"
        if (route.officialName.isBlank()) errors += "route.officialName must not be blank"
        if (!route.totalDistanceKm.isFinite() || route.totalDistanceKm <= 0.0) {
            errors += "route.totalDistanceKm must be finite and > 0"
        }
        if (route.source.isBlank()) errors += "route.source must not be blank"
        if (route.updatedAt.isBlank()) errors += "route.updatedAt must not be blank"

        val geometry = route.geometry.points
        if (geometry.size < 2) errors += "route.geometry must contain at least two points"
        var geometryCoordinatesValid = true
        geometry.forEachIndexed { index, point ->
            if (!point.latitude.isFinite() || point.latitude !in -90.0..90.0) {
                geometryCoordinatesValid = false
                errors += "geometry[$index].latitude must be finite and within -90..90"
            }
            if (!point.longitude.isFinite() || point.longitude !in -180.0..180.0) {
                geometryCoordinatesValid = false
                errors += "geometry[$index].longitude must be finite and within -180..180"
            }
            if (index > 0 && point == geometry[index - 1]) {
                errors += "geometry[$index] duplicates previous point"
            }
        }

        if (
            geometry.size >= 2 &&
            geometryCoordinatesValid &&
            route.totalDistanceKm.isFinite() &&
            route.totalDistanceKm > 0.0
        ) {
            val measuredKm = RouteGeometryMetrics.lengthKm(route.geometry)
            val allowedDifference = maxOf(
                DISTANCE_TOLERANCE_KM,
                route.totalDistanceKm * GEOMETRY_DISTANCE_TOLERANCE_RATIO
            )
            if (abs(measuredKm - route.totalDistanceKm) > allowedDifference) {
                errors += "route geometry length $measuredKm km differs from declared total distance ${route.totalDistanceKm} km by more than $allowedDifference km"
            }
        }

        val seenIds = mutableSetOf<String>()
        val seenNumbers = mutableSetOf<Int>()
        var previousEnd = 0.0

        route.stages.forEachIndexed { index, stage ->
            if (stage.id.isBlank()) errors += "stage[$index].id must not be blank"
            if (!seenIds.add(stage.id)) errors += "duplicate stage id: ${stage.id}"
            if (stage.routeId != route.id) errors += "stage ${stage.id} has routeId different from route.id"
            if (stage.number <= 0) errors += "stage ${stage.id}.number must be > 0"
            if (!seenNumbers.add(stage.number)) errors += "duplicate stage number: ${stage.number}"
            if (stage.name.isBlank()) errors += "stage ${stage.id}.name must not be blank"
            if (stage.startName.isBlank()) errors += "stage ${stage.id}.startName must not be blank"
            if (stage.endName.isBlank()) errors += "stage ${stage.id}.endName must not be blank"
            if (stage.source.isBlank()) errors += "stage ${stage.id}.source must not be blank"
            if (!stage.startRouteKm.isFinite()) errors += "stage ${stage.id}.startRouteKm must be finite"
            if (!stage.endRouteKm.isFinite()) errors += "stage ${stage.id}.endRouteKm must be finite"
            if (!stage.distanceKm.isFinite()) errors += "stage ${stage.id}.distanceKm must be finite"
            if (stage.startRouteKm.isFinite() && stage.startRouteKm < 0.0) errors += "stage ${stage.id}.startRouteKm must be >= 0"
            if (stage.endRouteKm.isFinite() && stage.startRouteKm.isFinite() && stage.endRouteKm <= stage.startRouteKm) {
                errors += "stage ${stage.id}.endRouteKm must be > startRouteKm"
            }
            if (stage.distanceKm.isFinite() && stage.distanceKm <= 0.0) errors += "stage ${stage.id}.distanceKm must be > 0"
            if (stage.endRouteKm.isFinite() && route.totalDistanceKm.isFinite() && stage.endRouteKm > route.totalDistanceKm) {
                errors += "stage ${stage.id} exceeds route total distance"
            }
            if (stage.startRouteKm.isFinite() && previousEnd.isFinite() && stage.startRouteKm + DISTANCE_TOLERANCE_KM < previousEnd) {
                errors += "stage ${stage.id} is out of route order"
            }
            if (stage.startRouteKm.isFinite() && stage.endRouteKm.isFinite() && stage.distanceKm.isFinite() &&
                abs(stage.distanceKm - (stage.endRouteKm - stage.startRouteKm)) > DISTANCE_TOLERANCE_KM
            ) {
                errors += "stage ${stage.id}.distanceKm differs from route-km interval by more than $DISTANCE_TOLERANCE_KM km"
            }
            if (stage.endRouteKm.isFinite()) {
                previousEnd = maxOf(previousEnd, stage.endRouteKm)
            }
        }

        return errors
    }

    fun requireValid(route: Route): Route {
        val errors = validate(route)
        require(errors.isEmpty()) { "Invalid route data: ${errors.joinToString("; ")}" }
        return route
    }
}
