package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/** Stable JSON boundary for the persisted V1 walking session plan. */
object WalkJsonCodec {
    private const val VERSION = 2

    fun encode(walk: Walk): String = JSONObject().apply {
        put("version", VERSION)
        put("id", walk.id)
        put("routeId", walk.routeId)
        putNullable("plannedStartKm", walk.plannedStartKm)
        putNullable("plannedDestinationKm", walk.plannedDestinationKm)
        putNullable("actualStartKm", walk.actualStartKm)
        putNullable("actualEndKm", walk.actualEndKm)
        putNullable("startedAt", walk.startedAt?.toString())
        putNullable("endedAt", walk.endedAt?.toString())
        put("status", walk.status.name)
        put("stageIds", walk.stageIds.joinToString("\u001f"))
        put("preparation", JSONObject().apply {
            put("audioMode", walk.preparation.audioMode.name)
            put("mapOrientation", walk.preparation.mapOrientation.name)
            put("intelligentBreaksEnabled", walk.preparation.intelligentBreaksEnabled)
            putNullable("customBreakTimeMinutes", walk.preparation.customBreakTimeMinutes)
            putNullable("customBreakDistanceKm", walk.preparation.customBreakDistanceKm)
            put("visibleApoiCategories", JSONArray().apply {
                walk.preparation.visibleApoiCategories.sortedBy(ApoiCategory::name).forEach { put(it.name) }
            })
        })
    }.toString()

    fun decode(json: String): Walk? = runCatching {
        val root = JSONObject(json)
        val version = root.optInt("version", 1)
        require(version == 1 || version == VERSION) { "Unsupported walking plan version: $version" }

        val id = root.getString("id").takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("walk id must not be blank")
        val routeId = root.getString("routeId").takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("routeId must not be blank")
        val plannedStartKm = root.optDoubleOrNull("plannedStartKm")
        val plannedDestinationKm = root.optDoubleOrNull("plannedDestinationKm")
        val actualStartKm = root.optDoubleOrNull("actualStartKm")
        val actualEndKm = root.optDoubleOrNull("actualEndKm")
        listOf(plannedStartKm, plannedDestinationKm, actualStartKm, actualEndKm).forEach { value ->
            require(value == null || (value.isFinite() && value >= 0.0))
        }
        val startedAt = root.optStringOrNull("startedAt")?.let(Instant::parse)
        val endedAt = root.optStringOrNull("endedAt")?.let(Instant::parse)
        if (startedAt != null && endedAt != null) require(!endedAt.isBefore(startedAt))

        val preparation = decodePreparation(root.optJSONObject("preparation"))

        Walk(
            id = id,
            routeId = routeId,
            plannedStartKm = plannedStartKm,
            plannedDestinationKm = plannedDestinationKm,
            actualStartKm = actualStartKm,
            actualEndKm = actualEndKm,
            startedAt = startedAt,
            endedAt = endedAt,
            status = WalkStatus.valueOf(root.getString("status")),
            stageIds = root.optString("stageIds")
                .split("\u001f")
                .map(String::trim)
                .filter(String::isNotEmpty),
            preparation = preparation
        )
    }.getOrNull()

    private fun decodePreparation(json: JSONObject?): WalkingPreparationConfig {
        if (json == null) return WalkingPreparationConfig()
        val visible = buildSet {
            val array = json.optJSONArray("visibleApoiCategories") ?: return@buildSet
            for (i in 0 until array.length()) {
                runCatching { ApoiCategory.valueOf(array.getString(i)) }.getOrNull()?.let(::add)
            }
        }
        return WalkingPreparationConfig(
            audioMode = runCatching { AudioMode.valueOf(json.optString("audioMode")) }.getOrDefault(AudioMode.NORMAL),
            mapOrientation = runCatching { MapOrientation.valueOf(json.optString("mapOrientation")) }.getOrDefault(MapOrientation.NORTH),
            intelligentBreaksEnabled = json.optBoolean("intelligentBreaksEnabled", true),
            customBreakTimeMinutes = json.optIntOrNull("customBreakTimeMinutes"),
            customBreakDistanceKm = json.optDoubleOrNull("customBreakDistanceKm"),
            visibleApoiCategories = visible
        )
    }

    private fun JSONObject.putNullable(key: String, value: Any?) = put(key, value ?: JSONObject.NULL)
    private fun JSONObject.optDoubleOrNull(key: String): Double? = if (isNull(key)) null else optDouble(key)
    private fun JSONObject.optStringOrNull(key: String): String? = if (isNull(key)) null else optString(key)
    private fun JSONObject.optIntOrNull(key: String): Int? = if (isNull(key) || !has(key)) null else optInt(key)
}
