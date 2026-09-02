package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiDataset
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.json.JSONObject

/** Deterministic parser for the published V1 APOI contract. */
object ApoiJsonParser {
    fun parse(json: String): ApoiDataset {
        val root = JSONObject(json)
        val recordsJson = root.getJSONArray("apoi")
        val records = buildList(recordsJson.length()) {
            for (i in 0 until recordsJson.length()) {
                val item = recordsJson.getJSONObject(i)
                add(parseApoi(item))
            }
        }
        val dataset = ApoiDataset(
            datasetVersion = root.getString("dataset_version"),
            environment = root.getString("environment"),
            apoi = records
        )
        val errors = ApoiValidator.validateDataset(dataset.environment, dataset.apoi)
        require(errors.isEmpty()) { "Invalid APOI dataset: ${errors.joinToString("; ")}" }
        return dataset
    }

    private fun parseApoi(item: JSONObject): Apoi {
        val servicesJson = item.getJSONArray("services")
        val services = buildSet(servicesJson.length()) {
            for (i in 0 until servicesJson.length()) {
                add(ApoiCategory.valueOf(servicesJson.getString(i)))
            }
        }
        val locationJson = item.getJSONObject("location")
        val publicationJson = item.getJSONObject("publication")
        return Apoi(
            id = item.getString("id"),
            name = item.getString("name"),
            description = item.optString("description").takeIf { it.isNotBlank() },
            mainCategory = ApoiCategory.valueOf(item.getString("main_category")),
            services = services,
            location = ApoiLocation(
                latitude = locationJson.optDoubleOrNull("latitude"),
                longitude = locationJson.optDoubleOrNull("longitude"),
                precision = LocationPrecision.valueOf(locationJson.getString("precision")),
                locality = locationJson.optStringOrNull("locality"),
                municipality = locationJson.optStringOrNull("municipality"),
                reference = locationJson.optStringOrNull("reference"),
                routeId = locationJson.optStringOrNull("route_id"),
                routeKm = locationJson.optDoubleOrNull("route_km"),
                distanceToRouteM = locationJson.optDoubleOrNull("distance_to_route_m"),
                accessDistanceM = locationJson.optDoubleOrNull("access_distance_m"),
                routeRelation = RouteRelation.valueOf(locationJson.getString("route_relation"))
            ),
            publication = ApoiPublication(
                status = PublicationStatus.valueOf(publicationJson.getString("status")),
                reason = publicationJson.optStringOrNull("reason")
            )
        )
    }

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

    private fun JSONObject.optDoubleOrNull(name: String): Double? =
        if (has(name) && !isNull(name)) optDouble(name).takeUnless { it.isNaN() } else null
}
