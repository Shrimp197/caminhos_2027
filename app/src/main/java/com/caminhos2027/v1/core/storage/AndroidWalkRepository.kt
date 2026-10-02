package com.caminhos2027.v1.core.storage

import android.content.Context
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.walking.WalkJsonCodec
import com.caminhos2027.v1.core.walking.WalkRepository
import org.json.JSONArray

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
                    WalkJsonCodec.decode(array.getJSONObject(index).toString())?.let(::add)
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun writeAll(walks: List<Walk>) {
        val array = JSONArray()
        walks.forEach { walk -> array.put(org.json.JSONObject(WalkJsonCodec.encode(walk))) }
        preferences.edit().putString(KEY_WALKS, array.toString()).commit()
    }

    private companion object {
        const val PREFS = "walking_v1"
        const val KEY_WALKS = "walks"
    }
}
