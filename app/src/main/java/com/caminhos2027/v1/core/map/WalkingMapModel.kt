package com.caminhos2027.v1.core.map

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RoutePosition

/** Prepared map data. The map renderer receives data; it does not decide publication rules. */
data class WalkingMapModel(
    val routeGeometry: List<GeoPoint>,
    val position: RoutePosition?,
    val markers: List<MapApoiMarker> = emptyList()
) {
    val hasOfficialGeometry: Boolean
        get() = routeGeometry.size >= 2
}

data class MapApoiMarker(
    val apoiId: String,
    val name: String,
    val routeKm: Double?,
    val apoi: Apoi
)

object WalkingMapModelBuilder {
    fun build(
        routeGeometry: List<GeoPoint>,
        position: RoutePosition?,
        apoi: List<Apoi> = emptyList()
    ): WalkingMapModel = WalkingMapModel(
        routeGeometry = routeGeometry,
        position = position,
        markers = apoi
            .mapNotNull { record ->
                record.location.routeKm?.let { km ->
                    MapApoiMarker(record.id, record.name, km, record)
                }
            }
            .sortedBy { it.routeKm }
    )
}
