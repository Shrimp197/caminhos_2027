package com.caminhos2027.v1.core.storage

import android.content.Context
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.walking.WalkRepository
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/** Small local persistence adapter for walking sessions. No network and no database are required. */
class AndroidWalkRepository(context: Context) : WalkRepository {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun save(walk: Walk) {
        val current = readAll().associateBy { it.id }.toMutableMap()
        current[walk.id] = walk
        writeAll(current.values.toList())
    }

    override fun getById(id: String): Walk? = readAll().firstOrNull { it.id == id }

    override fun getActive(): Walk? = readAll().firstOrNull { it.status == WalkStatus.ACTIVE }

    override fun list(): List<Walk> = readAll()

    private fun readAll(): List<Walk> {
        val raw = preferences.getString(KEY_WALKS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    add(decode(array.getJSONObject(index)))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun writeAll(walks: List<Walk>) {
        val array = JSONArray()
        walks.forEach { array.put(encode(it)) }
        preferences.edit().putString(KEY_WALKS, array.toString()).apply()
    }

    private fun encode(walk: Walk): JSONObject = JSONObject().apply {
        put("id", walk.id)
        put("routeId", walk.routeId)
        putNullable("plannedStartKm", walk.plannedStartKm)
        putNullable("plannedDestinationKm", walk.plannedDestinationKm)
        putNullable("actualStartKm", walk.actualStartKm)
        putNullable("actualEndKm", walk.actualEndKm)
        putNullable("startedAt", walk.startedAt?.toString())
        putNullable("endedAt", walk.endedAt?.toString())
        put("status", walk.status.name)
        put("stageIds", JSONArray(walk.stageIds))
    }

    private fun decode(json: JSONObject): Walk = Walk(
        id = json.getString("id"),
        routeId = json.getString("routeId"),
        plannedStartKm = json.optNullableDouble("plannedStartKm"),
        plannedDestinationKm = json.optNullableDouble("plannedDestinationKm"),
        actualStartKm = json.optNullableDouble("actualStartKm"),
        actualEndKm = json.optNullableDouble("actualEndKm"),
        startedAt = json.optNullableString("startedAt")?.let(Instant::parse),
        endedAt = json.optNullableString("endedAt")?.let(Instant::parse),
        status = WalkStatus.valueOf(json.getString("status")),
        stageIds = json.optJSONArray("stageIds")?.let { array ->
            buildList { for (index in 0 until array.length()) add(array.getString(index)) }
        } ?: emptyList()
    )

    private fun JSONObject.putNullable(key: String, value: Any?) {
        if (value == null) put(key, JSONObject.NULL) else put(key, value)
    }

    private fun JSONObject.optNullableDouble(key: String): Double? =
        if (isNull(key)) null else optDouble(key)

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key)

    private companion object {
        const val PREFS = "walking_v1"
        const val KEY_WALKS = "walks"
    }
}
