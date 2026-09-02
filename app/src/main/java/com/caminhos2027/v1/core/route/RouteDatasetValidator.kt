package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Route

/**
 * Structural and consistency checks applied before a route dataset can be exposed to the app.
 * This is intentionally independent of Android and does not decide whether a source is official.
 */
object RouteDatasetValidator {
    private const val MIN_TOTAL_DISTANCE_KM = 0.1
    private const val DISTANCE_TOLERANCE_RATIO = 0.15
    private const val MIN_DISTANCE_TOLERANCE_KM = 0.1

    fun validate(route: Route): ValidationResult {
        val errors = mutableListOf<String>()

        if (route.id.isBlank()) errors += "Route id is required"
        if (route.name.isBlank()) errors += "Route name is required"
        if (route.officialName.isBlank()) errors += "Official route name is required"
        if (route.source.isBlank()) errors += "Route source is required"
        if (route.updatedAt.isBlank()) errors += "Route updatedAt is required"
        if (route.totalDistanceKm < MIN_TOTAL_DISTANCE_KM) {
            errors += "Route total distance must be greater than ${MIN_TOTAL_DISTANCE_KM} km"
        }

        if (route.geometry.points.size < 2) {
            errors += "Route geometry must contain at least two points"
        }

        route.geometry.points.forEachIndexed { index, point ->
            if (point.latitude !in -90.0..90.0) errors += "Invalid latitude at point $index"
            if (point.longitude !in -180.0..180.0) errors += "Invalid longitude at point $index"
        }

        val geometryDistanceKm = if (route.geometry.points.size >= 2) {
            RouteGeometryMetrics.lengthKm(route.geometry)
        } else {
            0.0
        }
        val toleranceKm = maxOf(
            MIN_DISTANCE_TOLERANCE_KM,
            route.totalDistanceKm * DISTANCE_TOLERANCE_RATIO
        )
        if (route.geometry.points.size >= 2 &&
            kotlin.math.abs(geometryDistanceKm - route.totalDistanceKm) > toleranceKm
        ) {
            errors += "Geometry distance ${"%.3f".format(geometryDistanceKm)} km differs from declared ${"%.3f".format(route.totalDistanceKm)} km"
        }

        validateStages(route, errors)

        return ValidationResult(
            valid = errors.isEmpty(),
            errors = errors,
            geometryDistanceKm = geometryDistanceKm
        )
    }

    private fun validateStages(route: Route, errors: MutableList<String>) {
        var previousEnd = 0.0
        route.stages.forEachIndexed { index, stage ->
            if (stage.id.isBlank()) errors += "Stage $index id is required"
            if (stage.routeId != route.id) errors += "Stage $index routeId does not match route"
            if (stage.number <= 0) errors += "Stage $index number must be positive"
            if (stage.startRouteKm < 0.0 || stage.endRouteKm > route.totalDistanceKm) {
                errors += "Stage $index is outside route bounds"
            }
            if (stage.endRouteKm <= stage.startRouteKm) {
                errors += "Stage $index must have endRouteKm greater than startRouteKm"
            }
            if (stage.distanceKm <= 0.0) errors += "Stage $index distance must be positive"
            if (index > 0 && stage.startRouteKm < previousEnd) {
                errors += "Stage $index overlaps the previous stage"
            }
            val rangeDistance = stage.endRouteKm - stage.startRouteKm
            if (kotlin.math.abs(rangeDistance - stage.distanceKm) > 0.1) {
                errors += "Stage $index distanceKm does not match its route range"
            }
            previousEnd = maxOf(previousEnd, stage.endRouteKm)
        }
    }
}

data class ValidationResult(
    val valid: Boolean,
    val errors: List<String>,
    val geometryDistanceKm: Double
)
