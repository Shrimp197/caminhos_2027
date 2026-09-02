package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.json.JSONObject

/** Loads a published route dataset from the app's local assets. */
class AssetRouteDataSource(
    private val context: Context,
    private val assetPath: String
) : RouteDataSource {
    override fun loadRoute(): Route {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return RouteJsonParser.parse(json)
    }
}

/** Small, deterministic parser for the V1 route contract. */
object RouteJsonParser {
    fun parse(json: String): Route {
        val root = JSONObject(json)
        val geometryJson = root.getJSONObject("geometry")
        require(geometryJson.getString("type") == "LineString") {
            "Route geometry must be a GeoJSON LineString"
        }

        val coordinates = geometryJson.getJSONArray("coordinates")
        val points = buildList(coordinates.length()) {
            for (i in 0 until coordinates.length()) {
                val coordinate = coordinates.getJSONArray(i)
                require(coordinate.length() >= 2) { "Geometry coordinate[$i] must contain longitude and latitude" }
                add(
                    GeoPoint(
                        latitude = coordinate.getDouble(1),
                        longitude = coordinate.getDouble(0)
                    )
                )
            }
        }

        val stagesJson = root.getJSONArray("stages")
        val stages = buildList(stagesJson.length()) {
            for (i in 0 until stagesJson.length()) {
                val item = stagesJson.getJSONObject(i)
                add(
                    Stage(
                        id = item.getString("id"),
                        routeId = item.getString("route_id"),
                        number = item.getInt("number"),
                        name = item.getString("name"),
                        startRouteKm = item.getDouble("start_route_km"),
                        endRouteKm = item.getDouble("end_route_km"),
                        distanceKm = item.getDouble("distance_km"),
                        startName = item.getString("start_name"),
                        endName = item.getString("end_name"),
                        source = item.getString("source")
                    )
                )
            }
        }

        return Route(
            id = root.getString("id"),
            name = root.getString("name"),
            officialName = root.getString("official_name"),
            totalDistanceKm = root.getDouble("total_distance_km"),
            source = root.getString("source"),
            updatedAt = root.getString("updated_at"),
            geometry = RouteGeometry(points),
            stages = stages
        )
    }
}
