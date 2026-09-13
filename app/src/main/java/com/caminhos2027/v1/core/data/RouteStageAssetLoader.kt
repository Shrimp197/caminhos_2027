package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage
import org.json.JSONObject

/** Loads route stage reference data independently from route geometry. Stage distances may be approximate. */
class RouteStageAssetLoader(private val context: Context) {
    fun enrich(route: Route, assetPath: String = "data/route-stages.json"): Route {
        val root = context.assets.open(assetPath).bufferedReader().use { JSONObject(it.readText()) }
        val routes = root.optJSONArray("routes") ?: return route
        val routeJson = (0 until routes.length())
            .map { routes.getJSONObject(it) }
            .firstOrNull { it.optString("route_id") == route.id }
            ?: return route
        val stageArray = routeJson.optJSONArray("stages") ?: return route

        val stages = mutableListOf<Stage>()
        var cumulativeKm = 0.0
        for (i in 0 until stageArray.length()) {
            val item = stageArray.getJSONObject(i)
            val distance = item.optDouble("distance_km", Double.NaN)
            require(distance.isFinite() && distance > 0.0) { "Stage distance must be positive" }
            val startKm = cumulativeKm
            val endKm = if (i == stageArray.length() - 1) route.totalDistanceKm else (cumulativeKm + distance).coerceAtMost(route.totalDistanceKm)
            stages += Stage(
                id = item.getString("id"),
                routeId = route.id,
                number = item.getInt("number"),
                name = item.getString("name"),
                startRouteKm = startKm,
                endRouteKm = endKm,
                distanceKm = distance,
                startName = item.getString("start_name"),
                endName = item.getString("end_name"),
                source = item.optString("source_note").ifBlank { routeJson.optString("source") }
            )
            cumulativeKm += distance
        }
        return route.copy(stages = stages)
    }
}
