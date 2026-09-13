package com.caminhos2027.v1.core.model

/** Official route reference. Immutable from the pilgrim's point of view. */
data class Route(
    val id: String,
    val name: String,
    val officialName: String,
    val totalDistanceKm: Double,
    val source: String,
    val updatedAt: String?,
    val geometry: RouteGeometry,
    val stages: List<Stage>
)

/** Ordered route geometry normalized from the official GPX/KML source. */
data class RouteGeometry(
    val points: List<GeoPoint>
)

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class Stage(
    val id: String,
    val routeId: String,
    val number: Int,
    val name: String,
    val startRouteKm: Double,
    val endRouteKm: Double,
    val distanceKm: Double,
    val startName: String,
    val endName: String,
    val source: String
)
