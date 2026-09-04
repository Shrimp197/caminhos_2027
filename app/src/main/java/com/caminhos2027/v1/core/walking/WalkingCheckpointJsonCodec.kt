package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.route.GpsState
import org.json.JSONObject
import java.time.Instant

/** Stable, defensive JSON boundary for the persisted V1 walking checkpoint. */
object WalkingCheckpointJsonCodec {
    private const val VERSION = 1

    fun encode(checkpoint: WalkingCheckpoint): String = JSONObject().apply {
        put("version", VERSION)
        put("routePosition", checkpoint.routePosition?.toJson())
        put("gpsState", checkpoint.gpsState.name)
        put("isOffline", checkpoint.isOffline)
        putNullable("lastObservedAt", checkpoint.lastObservedAt?.toString())
    }.toString()

    fun decode(json: String): WalkingCheckpoint? = runCatching {
        val root = JSONObject(json)
        val versionValue = if (root.has("version")) root.get("version") else VERSION
        require(versionValue is Number && versionValue.toDouble() == VERSION.toDouble()) {
            "Unsupported walking checkpoint version type"
        }
        require(versionValue.toInt() == VERSION) {
            "Unsupported walking checkpoint version: $versionValue"
        }

        val position = root.optJSONObject("routePosition")?.let(::routePositionFromJson)
        val gpsState = GpsState.valueOf(root.getString("gpsState"))
        val lastObservedAt = root.optStringOrNull("lastObservedAt")?.let(Instant::parse)
        WalkingCheckpoint(
            routePosition = position,
            gpsState = gpsState,
            isOffline = root.optBoolean("isOffline", false),
            lastObservedAt = lastObservedAt
        )
    }.getOrNull()

    private fun RoutePosition.toJson() = JSONObject().apply {
        put("routeId", routeId)
        put("routeKm", routeKm)
        put("distanceToRouteMeters", distanceToRouteMeters)
        putNullable("stageId", stageId)
        put("confidence", confidence.name)
    }

    private fun routePositionFromJson(json: JSONObject): RoutePosition? {
        val routeId = json.getString("routeId").takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("routeId must not be blank")
        val routeKm = json.getDouble("routeKm")
        val distance = json.getDouble("distanceToRouteMeters")
        if (!routeKm.isFinite() || !distance.isFinite()) return null
        if (routeKm < 0.0 || distance < 0.0) return null
        return RoutePosition(
            routeId = routeId,
            routeKm = routeKm,
            distanceToRouteMeters = distance,
            stageId = json.optStringOrNull("stageId")?.takeIf { it.isNotBlank() },
            confidence = PositionConfidence.valueOf(json.getString("confidence"))
        )
    }

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key)
}
