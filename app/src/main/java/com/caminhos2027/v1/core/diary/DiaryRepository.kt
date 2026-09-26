package com.caminhos2027.v1.core.diary

import android.content.Context
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.GeoPoint
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val createdAt: Instant = Instant.now(),
    val walkId: String? = null,
    val routeKm: Double? = null,
    val location: GeoPoint? = null,
    val photoUri: String? = null
)

class DiaryRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<DiaryEntry> {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val location = item.optJSONObject("location")?.let {
                        GeoPoint(
                            latitude = it.getDouble("latitude"),
                            longitude = it.getDouble("longitude")
                        )
                    }
                    add(
                        DiaryEntry(
                            id = item.getString("id"),
                            content = item.getString("content"),
                            createdAt = Instant.parse(item.getString("created_at")),
                            walkId = item.optString("walk_id").takeIf { it.isNotBlank() },
                            routeKm = if (item.has("route_km") && !item.isNull("route_km")) item.getDouble("route_km") else null,
                            location = location,
                            photoUri = item.optString("photo_uri").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }.sortedByDescending { it.createdAt }
        }.getOrElse { emptyList() }
    }

    fun add(entry: DiaryEntry): List<DiaryEntry> {
        val updated = (load() + entry).sortedByDescending { it.createdAt }
        saveAll(updated)
        return updated
    }

    private fun saveAll(entries: List<DiaryEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            val json = JSONObject()
                .put("id", entry.id)
                .put("content", entry.content)
                .put("created_at", entry.createdAt.toString())
                .put("walk_id", entry.walkId ?: JSONObject.NULL)
                .put("route_km", entry.routeKm ?: JSONObject.NULL)
                .put("photo_uri", entry.photoUri ?: JSONObject.NULL)
            entry.location?.let {
                json.put(
                    "location",
                    JSONObject()
                        .put("latitude", it.latitude)
                        .put("longitude", it.longitude)
                )
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_ENTRIES, array.toString()).commit()
    }

    companion object {
        private const val PREFS_NAME = "peregrino_diary"
        private const val KEY_ENTRIES = "entries"
    }
}
