package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCapacity
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiCharacteristics
import com.caminhos2027.v1.core.model.ApoiConfidence
import com.caminhos2027.v1.core.model.ApoiContact
import com.caminhos2027.v1.core.model.ApoiCost
import com.caminhos2027.v1.core.model.ApoiCostModel
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.ApoiReservation
import com.caminhos2027.v1.core.model.ApoiReservationPolicy
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.SleepingType
import org.json.JSONArray
import org.json.JSONObject

/** Parses the shared APOI JSON contract into domain records. */
object ApoiJsonParser {
    fun parseDataset(json: String): List<Apoi> {
        val root = JSONObject(json)
        val items = root.optJSONArray("items") ?: JSONArray()
        return buildList(items.length()) {
            for (i in 0 until items.length()) add(parseItem(items.getJSONObject(i)))
        }
    }

    fun parseItem(item: JSONObject): Apoi = Apoi(
        id = item.getString("id"),
        name = item.getString("name"),
        description = item.optNullableString("description"),
        mainCategory = enumValue<ApoiCategory>(item.getString("main_category")),
        services = item.getJSONArray("services").toStringSet { enumValue<ApoiCategory>(it) },
        location = parseLocation(item.getJSONObject("location")),
        publication = parsePublication(item.optJSONObject("publication")),
        cost = parseCost(item.optJSONObject("cost")),
        reservation = parseReservation(item.optJSONObject("reservation")),
        availability = parseAvailability(item.optJSONObject("availability")),
        capacity = parseCapacity(item.optJSONObject("capacity")),
        characteristics = parseCharacteristics(item.optJSONObject("characteristics")),
        contact = parseContact(item.optJSONObject("contact")),
        confidence = parseConfidence(item.optJSONObject("confidence"))
    )

    private fun parseLocation(json: JSONObject) = ApoiLocation(
        latitude = json.optNullableDouble("latitude"),
        longitude = json.optNullableDouble("longitude"),
        precision = enumValue(json.optString("precision", LocationPrecision.UNKNOWN.name)),
        locality = json.optNullableString("locality"),
        municipality = json.optNullableString("municipality"),
        reference = json.optNullableString("reference"),
        routeId = json.optNullableString("route_id"),
        routeKm = json.optNullableDouble("route_km"),
        distanceToRouteM = json.optNullableDouble("distance_to_route_m"),
        accessDistanceM = json.optNullableDouble("access_distance_m"),
        routeRelation = enumValue(json.optString("route_relation", RouteRelation.LOCATION_UNCERTAIN.name))
    )

    private fun parsePublication(json: JSONObject?) = ApoiPublication(
        status = enumValue(json?.optString("status", PublicationStatus.REVIEW.name) ?: PublicationStatus.REVIEW.name),
        reason = json?.optNullableString("reason")
    )

    private fun parseCost(json: JSONObject?) = ApoiCost(
        model = enumValue(json?.optString("model", ApoiCostModel.UNKNOWN.name) ?: ApoiCostModel.UNKNOWN.name),
        amount = json?.optNullableDouble("amount"),
        currency = json?.optNullableString("currency"),
        description = json?.optNullableString("description")
    )

    private fun parseReservation(json: JSONObject?) = ApoiReservation(
        policy = enumValue(json?.optString("policy", ApoiReservationPolicy.UNKNOWN.name) ?: ApoiReservationPolicy.UNKNOWN.name),
        contact = json?.optNullableString("contact"),
        url = json?.optNullableString("url"),
        notes = json?.optNullableString("notes")
    )

    private fun parseAvailability(json: JSONObject?) = ApoiAvailability(
        status = enumValue(json?.optString("status", ApoiAvailabilityStatus.AWAITING_CONFIRMATION.name) ?: ApoiAvailabilityStatus.AWAITING_CONFIRMATION.name),
        validFrom = json?.optNullableString("valid_from"),
        validUntil = json?.optNullableString("valid_until"),
        recurrence = json?.optNullableString("recurrence"),
        season = json?.optNullableString("season"),
        openingHours = json?.optNullableString("opening_hours"),
        notes = json?.optNullableString("notes")
    )

    private fun parseCapacity(json: JSONObject?) = ApoiCapacity(
        total = json?.optNullableInt("total"),
        sleeping = json?.optNullableInt("sleeping"),
        notes = json?.optNullableString("notes")
    )

    private fun parseCharacteristics(json: JSONObject?) = ApoiCharacteristics(
        sleepingType = json?.optNullableString("sleeping_type")?.let { enumValue<SleepingType>(it) },
        shower = json?.optNullableBoolean("shower"),
        hotWater = json?.optNullableBoolean("hot_water"),
        wc = json?.optNullableBoolean("wc"),
        laundry = json?.optNullableBoolean("laundry"),
        drying = json?.optNullableBoolean("drying"),
        accessibility = json?.optNullableBoolean("accessibility"),
        notes = json?.optNullableString("notes")
    )

    private fun parseContact(json: JSONObject?) = ApoiContact(
        responsible = json?.optNullableString("responsible"),
        organization = json?.optNullableString("organization"),
        phone = json?.optNullableString("phone"),
        email = json?.optNullableString("email"),
        website = json?.optNullableString("website"),
        social = json?.optNullableString("social")
    )

    private fun parseConfidence(json: JSONObject?) = ApoiConfidence(
        overall = enumValue(json?.optString("overall", PositionConfidence.UNKNOWN.name) ?: PositionConfidence.UNKNOWN.name),
        location = enumValue(json?.optString("location", PositionConfidence.UNKNOWN.name) ?: PositionConfidence.UNKNOWN.name),
        support = enumValue(json?.optString("support", PositionConfidence.UNKNOWN.name) ?: PositionConfidence.UNKNOWN.name),
        availability = enumValue(json?.optString("availability", PositionConfidence.UNKNOWN.name) ?: PositionConfidence.UNKNOWN.name),
        criticalInformation = enumValue(json?.optString("critical_information", PositionConfidence.UNKNOWN.name) ?: PositionConfidence.UNKNOWN.name)
    )

    private inline fun <reified T : Enum<T>> enumValue(value: String): T =
        runCatching { enumValueOf<T>(value) }.getOrElse {
            throw IllegalArgumentException("Unknown ${T::class.simpleName} value: $value")
        }

    private fun JSONArray.toStringSet(mapper: (String) -> ApoiCategory): Set<ApoiCategory> =
        buildSet(length()) {
            for (i in 0 until length()) add(mapper(getString(i)))
        }

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun JSONObject.optNullableDouble(key: String): Double? =
        if (!has(key) || isNull(key)) null else getDouble(key)

    private fun JSONObject.optNullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else getInt(key)

    private fun JSONObject.optNullableBoolean(key: String): Boolean? =
        if (!has(key) || isNull(key)) null else getBoolean(key)
}
