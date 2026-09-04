package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.json.JSONObject
import java.time.Instant

/** Stable JSON boundary for the persisted V1 walking session plan. */
object WalkJsonCodec {
    private const val VERSION = 1

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
    }.toString()

    fun decode(json: String): Walk? = runCatching {
        val root = JSONObject(json)
        val versionValue = if (root.has("version")) root.get("version") else VERSION
        require(versionValue is Number && versionValue.toDouble() == VERSION.toDouble()) {
            "Unsupported walking plan version type"
        }
        require(versionValue.toInt() == VERSION) {
            "Unsupported walking plan version: $versionValue"
        }

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
                .filter(String::isNotEmpty)
        )
    }.getOrNull()

    private fun JSONObject.putNullable(key: String, value: Any?) = put(key, value ?: JSONObject.NULL)
    private fun JSONObject.optDoubleOrNull(key: String): Double? = if (isNull(key)) null else optDouble(key)
    private fun JSONObject.optStringOrNull(key: String): String? = if (isNull(key)) null else optString(key)
}
