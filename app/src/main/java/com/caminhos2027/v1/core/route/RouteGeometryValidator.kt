package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RouteGeometry

/** Structural validation for ordered route geometry before it can drive navigation. */
object RouteGeometryValidator {
    fun validate(geometry: RouteGeometry) {
        require(geometry.points.size >= 2) { "Route geometry must contain at least two points" }

        geometry.points.forEach { point ->
            require(point.latitude.isFinite() && point.longitude.isFinite()) {
                "Route geometry contains a non-finite coordinate"
            }
            require(point.latitude in -90.0..90.0) {
                "Route geometry contains an invalid latitude"
            }
            require(point.longitude in -180.0..180.0) {
                "Route geometry contains an invalid longitude"
            }
        }

        geometry.points.zipWithNext().forEach { (a, b) ->
            require(a != b) { "Route geometry contains consecutive duplicate points" }
        }
    }

    fun isValid(geometry: RouteGeometry): Boolean = runCatching { validate(geometry) }.isSuccess
}
