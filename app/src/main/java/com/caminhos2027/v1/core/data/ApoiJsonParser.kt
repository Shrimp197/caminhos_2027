package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiConfidence
import com.caminhos2027.v1.core.model.ApoiCost
import com.caminhos2027.v1.core.model.ApoiDataset
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.ApoiReservation
import com.caminhos2027.v1.core.model.ApoiSource
import com.caminhos2027.v1.core.model.ApoiSupport
import com.caminhos2027.v1.core.model.AvailabilityStatus
import com.caminhos2027.v1.core.model.ConfidenceLevel
import com.caminhos2027.v1.core.model.CostModel
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.ReservationPolicy
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.SleepingType
import org.json.JSONObject

/** Deterministic parser for the V1 APOI dataset contract. */
object ApoiJsonParser {
    fun parse(json: String): ApoiDataset {
        val root = JSONObject(json)
        val recordsJson = root.getJSONArray("apoi")
        val records = buildList(recordsJson.length()) {
            for (i in 0 until recordsJson.length()) add(parseApoi(recordsJson.getJSONObject(i)))
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
        val location = item.getJSONObject("location")
        val publication = item.getJSONObject("publication")
        return Apoi(
            id = item.getString("id"),
            name = item.getString("name"),
            description = item.optStringOrNull("description"),
            mainCategory = category(item.getString("main_category")),
            services = item.getJSONArray("services").toCategorySet(),
            location = ApoiLocation(
                latitude = location.optDoubleOrNull("latitude"),
                longitude = location.optDoubleOrNull("longitude"),
                precision = enumValue(location.getString("precision"), LocationPrecision.entries),
                locality = location.optStringOrNull("locality"),
                municipality = location.optStringOrNull("municipality"),
                reference = location.optStringOrNull("reference"),
                routeId = location.optStringOrNull("route_id"),
                routeKm = location.optDoubleOrNull("route_km"),
                distanceToRouteM = location.optDoubleOrNull("distance_to_route_m"),
                accessDistanceM = location.optDoubleOrNull("access_distance_m"),
                routeRelation = enumValue(location.getString("route_relation"), RouteRelation.entries)
            ),
            publication = ApoiPublication(
                status = enumValue(publication.getString("status"), PublicationStatus.entries),
                reason = publication.optStringOrNull("reason")
            ),
            availability = item.optJSONObject("availability")?.let { parseAvailability(it) } ?: ApoiAvailability(),
            cost = item.optJSONObject("cost")?.let { parseCost(it) } ?: ApoiCost(),
            reservation = item.optJSONObject("reservation")?.let { parseReservation(it) } ?: ApoiReservation(),
            support = item.optJSONObject("support")?.let { parseSupport(it) } ?: ApoiSupport(),
            confidence = item.optJSONObject("confidence")?.let { parseConfidence(it) } ?: ApoiConfidence(),
            sources = item.optJSONArray("sources")?.toSources() ?: emptyList()
        )
    }

    private fun parseAvailability(json: JSONObject) = ApoiAvailability(
        status = enumValue(json.optString("status", AvailabilityStatus.AWAITING_CONFIRMATION.name), AvailabilityStatus.entries),
        validFrom = json.optStringOrNull("valid_from"),
        validUntil = json.optStringOrNull("valid_until"),
        recurrence = json.optStringOrNull("recurrence"),
        season = json.optStringOrNull("season"),
        openingHours = json.optStringOrNull("opening_hours"),
        notes = json.optStringOrNull("notes")
    )

    private fun parseCost(json: JSONObject) = ApoiCost(
        model = enumValue(json.optString("model", CostModel.UNKNOWN.name), CostModel.entries),
        amount = json.optDoubleOrNull("amount"),
        currency = json.optStringOrNull("currency"),
        description = json.optStringOrNull("description")
    )

    private fun parseReservation(json: JSONObject) = ApoiReservation(
        policy = enumValue(json.optString("policy", ReservationPolicy.UNKNOWN.name), ReservationPolicy.entries),
        contact = json.optStringOrNull("contact"),
        url = json.optStringOrNull("url"),
        notes = json.optStringOrNull("notes")
    )

    private fun parseSupport(json: JSONObject) = ApoiSupport(
        pilgrimSupportConfirmed = json.optBooleanOrNull("pilgrim_support_confirmed"),
        capacity = json.optIntOrNull("capacity"),
        sleepingType = json.optStringOrNull("sleeping_type")?.let { enumValue(it, SleepingType.entries) },
        shower = json.optBooleanOrNull("shower"),
        hotWater = json.optBooleanOrNull("hot_water"),
        waterAvailable = json.optBooleanOrNull("water_available"),
        waterPotable = json.optBooleanOrNull("water_potable"),
        waterPotableConfirmed = json.optBooleanOrNull("water_potable_confirmed"),
        foodAvailable = json.optBooleanOrNull("food_available"),
        chargingAvailable = json.optBooleanOrNull("charging_available"),
        transportAvailable = json.optBooleanOrNull("transport_available"),
        emergencyAvailable = json.optBooleanOrNull("emergency_available"),
        wc = json.optBooleanOrNull("wc"),
        laundry = json.optBooleanOrNull("laundry"),
        drying = json.optBooleanOrNull("drying")
    )

    private fun parseConfidence(json: JSONObject) = ApoiConfidence(
        overall = enumValue(json.optString("overall", ConfidenceLevel.UNKNOWN.name), ConfidenceLevel.entries),
        location = enumValue(json.optString("location", ConfidenceLevel.UNKNOWN.name), ConfidenceLevel.entries),
        support = enumValue(json.optString("support", ConfidenceLevel.UNKNOWN.name), ConfidenceLevel.entries),
        availability = enumValue(json.optString("availability", ConfidenceLevel.UNKNOWN.name), ConfidenceLevel.entries),
        criticalInformation = enumValue(json.optString("critical_information", ConfidenceLevel.UNKNOWN.name), ConfidenceLevel.entries)
    )

    private fun JSONArrayToSources(json: org.json.JSONArray): List<ApoiSource> = json.toSources()

    private fun org.json.JSONArray.toSources(): List<ApoiSource> = buildList(length()) {
        for (i in 0 until length()) {
            val source = getJSONObject(i)
            add(ApoiSource(
                id = source.getString("id"),
                type = source.getString("type"),
                name = source.getString("name"),
                reference = source.optStringOrNull("reference"),
                url = source.optStringOrNull("url"),
                collectedAt = source.optStringOrNull("collected_at"),
                publishedAt = source.optStringOrNull("published_at"),
                verifiedAt = source.optStringOrNull("verified_at"),
                method = source.optStringOrNull("method"),
                notes = source.optStringOrNull("notes")
            ))
        }
    }

    private fun org.json.JSONArray.toCategorySet(): Set<ApoiCategory> = buildSet(length()) {
        for (i in 0 until length()) add(category(getString(i)))
    }

    private fun category(value: String): ApoiCategory = enumValue(value, ApoiCategory.entries)

    private inline fun <reified T : Enum<T>> enumValue(value: String, values: List<T>): T {
        return values.firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: error("Unknown ${T::class.simpleName} value: $value")
    }

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

    private fun JSONObject.optDoubleOrNull(name: String): Double? =
        if (has(name) && !isNull(name)) optDouble(name).takeUnless { it.isNaN() } else null

    private fun JSONObject.optIntOrNull(name: String): Int? =
        if (has(name) && !isNull(name)) optInt(name) else null

    private fun JSONObject.optBooleanOrNull(name: String): Boolean? =
        if (has(name) && !isNull(name)) optBoolean(name) else null
}
