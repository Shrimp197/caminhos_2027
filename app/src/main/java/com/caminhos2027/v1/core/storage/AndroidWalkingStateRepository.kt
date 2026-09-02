package com.caminhos2027.v1.core.storage

import android.content.Context
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingCheckpoint
import com.caminhos2027.v1.core.walking.WalkingStateRepository
import org.json.JSONObject

/** Persists only the minimal walking checkpoint needed to resume safely after process death. */
class AndroidWalkingStateRepository(context: Context) : WalkingStateRepository {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun save(walkId: String, checkpoint: WalkingCheckpoint) {
        preferences.edit().putString(key(walkId), encode(checkpoint).toString()).apply()
    }

    override fun get(walkId: String): WalkingCheckpoint? {
        val raw = preferences.getString(key(walkId), null) ?: return null
        return runCatching { decode(JSONObject(raw)) }.getOrNull()
    }

    override fun clear(walkId: String) {
        preferences.edit().remove(key(walkId)).apply()
    }

    private fun encode(checkpoint: WalkingCheckpoint): JSONObject = JSONObject().apply {
        put("gpsState", checkpoint.gpsState.name)
        put("offline", checkpoint.isOffline)
        val position = checkpoint.routePosition
        if (position == null) {
            put("routePosition", JSONObject.NULL)
        } else {
            put("routePosition", JSONObject().apply {
                put("routeId", position.routeId)
                put("routeKm", position.routeKm)
                put("distanceToRouteMeters", position.distanceToRouteMeters)
                put("stageId", position.stageId ?: JSONObject.NULL)
                put("confidence", position.confidence.name)
            })
        }
    }

    private fun decode(json: JSONObject): WalkingCheckpoint {
        val positionJson = json.optJSONObject("routePosition")
        val position = positionJson?.let {
            RoutePosition(
                routeId = it.getString("routeId"),
                routeKm = it.getDouble("routeKm"),
                distanceToRouteMeters = it.getDouble("distanceToRouteMeters"),
                stageId = if (it.isNull("stageId")) null else it.optString("stageId"),
                confidence = PositionConfidence.valueOf(it.getString("confidence"))
            )
        }
        return WalkingCheckpoint(
            routePosition = position,
            gpsState = GpsState.valueOf(json.getString("gpsState")),
            isOffline = json.optBoolean("offline", false)
        )
    }

    private fun key(walkId: String) = "checkpoint_$walkId"

    private companion object {
        const val PREFS = "walking_v1"
    }
}
