package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.json.JSONObject

/** Decodes the stable V1 route JSON contract. Validation is deliberately separate. */
object RouteJsonDecoder {
    fun decode(json: String): Route {
        val root = JSONObject(json)
        val geometryJson = root.getJSONObject("geometry")
        val pointsJson = geometryJson.getJSONArray("points")
        val points = buildList(pointsJson.length()) {
            for (index in 0 until pointsJson.length()) {
                val point = pointsJson.getJSONObject(index)
                add(
                    GeoPoint(
                        latitude = point.getDouble("latitude"),
                        longitude = point.getDouble("longitude")
                    )
                )
            }
        }

        val stagesJson = root.optJSONArray("stages")
        val stages = buildList(stagesJson?.length() ?: 0) {
            if (stagesJson != null) {
                for (index in 0 until stagesJson.length()) {
                    val stage = stagesJson.getJSONObject(index)
                    add(
                        Stage(
                            id = stage.getString("id"),
                            routeId = stage.getString("routeId"),
                            number = stage.getInt("number"),
                            name = stage.getString("name"),
                            startRouteKm = stage.getDouble("startRouteKm"),
                            endRouteKm = stage.getDouble("endRouteKm"),
                            distanceKm = stage.getDouble("distanceKm"),
                            startName = stage.getString("startName"),
                            endName = stage.getString("endName"),
                            source = stage.getString("source")
                        )
                    )
                }
            }
        }

        return Route(
            id = root.getString("id"),
            name = root.getString("name"),
            officialName = root.getString("officialName"),
            totalDistanceKm = root.getDouble("totalDistanceKm"),
            source = root.getString("source"),
            updatedAt = root.getString("updatedAt"),
            geometry = RouteGeometry(points),
            stages = stages
        )
    }
}
