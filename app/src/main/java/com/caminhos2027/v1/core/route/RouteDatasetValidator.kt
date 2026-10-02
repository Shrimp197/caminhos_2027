package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.data.RouteValidator
import com.caminhos2027.v1.core.model.Route

/**
 * Dataset-level validation facade used by callers that also need measured geometry length.
 * The domain rules live in RouteValidator so route validation cannot silently diverge between layers.
 */
object RouteDatasetValidator {
    fun validate(route: Route): ValidationResult {
        val errors = RouteValidator.validate(route)
        val geometryCoordinatesValid = route.geometry.points.all {
            it.latitude.isFinite() && it.longitude.isFinite() &&
                it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0
        }
        val geometryDistanceKm = if (route.geometry.points.size >= 2 && geometryCoordinatesValid) {
            RouteGeometryMetrics.lengthKm(route.geometry)
        } else {
            0.0
        }

        return ValidationResult(
            valid = errors.isEmpty(),
            errors = errors,
            geometryDistanceKm = geometryDistanceKm
        )
    }
}

data class ValidationResult(
    val valid: Boolean,
    val errors: List<String>,
    val geometryDistanceKm: Double
)
