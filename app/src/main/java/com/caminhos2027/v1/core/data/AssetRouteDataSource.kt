package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.v1.core.model.Route
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
            stages = stages
        )
    }
}
