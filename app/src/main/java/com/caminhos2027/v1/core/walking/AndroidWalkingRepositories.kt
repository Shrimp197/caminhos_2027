package com.caminhos2027.v1.core.walking

import android.content.Context
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgress
import org.json.JSONObject
import java.time.Instant

/** Small Android persistence adapters for the current V1 walking session. */
class AndroidWalkRepository(context: Context) : WalkRepository {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun save(walk: Walk) {
        prefs.edit().putString(KEY_WALK, walk.toJson().toString()).apply()
    }

    override fun getById(id: String): Walk? = getStoredWalk()?.takeIf { it.id == id }

    override fun getActive(): Walk? = getStoredWalk()?.takeIf { it.status == WalkStatus.ACTIVE }

    override fun list(): List<Walk> = getStoredWalk()?.let(::listOf) ?: emptyList()

    private fun getStoredWalk(): Walk? = prefs.getString(KEY_WALK, null)?.let(::walkFromJson)

    companion object {
        private const val PREFS = "walking_v1"
        private const val KEY_WALK = "walk"
    }
}

/** Stores only the latest recoverable walking read state; derived values are restored as a snapshot. */
class AndroidWalkingStateRepository(context: Context) : WalkingStateRepository {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun save(state: WalkingState) {
        prefs.edit().putString(state.walk.id, state.toJson().toString()).apply()
    }

    override fun get(walkId: String): WalkingState? =
        prefs.getString(walkId, null)?.let { runCatching { walkingStateFromJson(JSONObject(it)) }.getOrNull() }

    override fun clear(walkId: String) {
        prefs.edit().remove(walkId).apply()
    }

    companion object { private const val PREFS = "walking_state_v1" }
}

private fun Walk.toJson() = JSONObject().apply {
    put("id", id)
    put("routeId", routeId)
    putNullable("plannedStartKm", plannedStartKm)
    putNullable("plannedDestinationKm", plannedDestinationKm)
    putNullable("actualStartKm", actualStartKm)
    putNullable("actualEndKm", actualEndKm)
    putNullable("startedAt", startedAt?.toString())
    putNullable("endedAt", endedAt?.toString())
    put("status", status.name)
    put("stageIds", stageIds.joinToString("\u001f"))
}

private fun walkFromJson(json: String): Walk? = runCatching {
    val o = JSONObject(json)
    Walk(
        id = o.getString("id"), routeId = o.getString("routeId"),
        plannedStartKm = o.optDoubleOrNull("plannedStartKm"),
        plannedDestinationKm = o.optDoubleOrNull("plannedDestinationKm"),
        actualStartKm = o.optDoubleOrNull("actualStartKm"), actualEndKm = o.optDoubleOrNull("actualEndKm"),
        startedAt = o.optStringOrNull("startedAt")?.let(Instant::parse),
        endedAt = o.optStringOrNull("endedAt")?.let(Instant::parse),
        status = WalkStatus.valueOf(o.getString("status")),
        stageIds = o.optString("stageIds").takeIf { it.isNotEmpty() }?.split("\u001f") ?: emptyList()
    )
}.getOrNull()

private fun WalkingState.toJson() = JSONObject().apply {
    put("walk", walk.toJson())
    put("routePosition", routePosition?.toJson())
    put("gpsState", gpsState.name)
    put("progress", progress?.toJson())
    put("nextApoi", JSONObject.NULL)
    put("nextApoiDistanceKm", JSONObject.NULL)
    put("isOffline", isOffline)
}

private fun RoutePosition.toJson() = JSONObject().apply {
    put("routeId", routeId); put("routeKm", routeKm); put("distanceToRouteMeters", distanceToRouteMeters)
    putNullable("stageId", stageId); put("confidence", confidence.name)
}

private fun WalkingProgress.toJson() = JSONObject().apply {
    put("currentRouteKm", currentRouteKm); put("walkedKm", walkedKm); put("remainingKm", remainingKm)
    put("targetRouteKm", targetRouteKm); put("progressRatio", progressRatio); putNullable("stageId", stageId)
}

private fun walkingStateFromJson(o: JSONObject): WalkingState {
    val positionJson = o.optJSONObject("routePosition")
    val progressJson = o.optJSONObject("progress")
    return WalkingState(
        walk = walkFromJson(o.getJSONObject("walk").toString())!!,
        routePosition = positionJson?.let { RoutePosition(it.getString("routeId"), it.getDouble("routeKm"), it.getDouble("distanceToRouteMeters"), it.optStringOrNull("stageId"), PositionConfidence.valueOf(it.getString("confidence"))) },
        gpsState = GpsState.valueOf(o.getString("gpsState")),
        progress = progressJson?.let { WalkingProgress(it.getDouble("currentRouteKm"), it.getDouble("walkedKm"), it.getDouble("remainingKm"), it.getDouble("targetRouteKm"), it.getDouble("progressRatio"), it.optStringOrNull("stageId")) },
        nextApoi = null,
        nextApoiDistanceKm = null,
        isOffline = o.optBoolean("isOffline", false)
    )
}

private fun JSONObject.putNullable(key: String, value: Any?) { put(key, value ?: JSONObject.NULL) }
private fun JSONObject.optDoubleOrNull(key: String): Double? = if (isNull(key)) null else optDouble(key)
private fun JSONObject.optStringOrNull(key: String): String? = if (isNull(key)) null else optString(key)
