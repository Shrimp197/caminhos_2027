package com.caminhos2027.v1.core.walking

import android.content.Context
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus

/** Small Android persistence adapters for the current V1 walking session. */
class AndroidWalkRepository(context: Context) : WalkRepository {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    override fun save(walk: Walk) { prefs.edit().putString(KEY_WALK, WalkJsonCodec.encode(walk)).apply() }
    override fun getById(id: String): Walk? = getStoredWalk()?.takeIf { it.id == id }
    override fun getActive(): Walk? = getStoredWalk()?.takeIf { it.status == WalkStatus.ACTIVE }
    override fun list(): List<Walk> = getStoredWalk()?.let(::listOf) ?: emptyList()
    private fun getStoredWalk(): Walk? = prefs.getString(KEY_WALK, null)?.let(WalkJsonCodec::decode)
    companion object { private const val PREFS = "walking_v1"; private const val KEY_WALK = "walk" }
}

/** Persists only the latest device-derived checkpoint; progress and APOI are rebuilt from route data. */
class AndroidWalkingStateRepository(context: Context) : WalkingStateRepository {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    override fun save(walkId: String, checkpoint: WalkingCheckpoint) {
        prefs.edit().putString(walkId, WalkingCheckpointJsonCodec.encode(checkpoint)).apply()
    }
    override fun get(walkId: String): WalkingCheckpoint? =
        prefs.getString(walkId, null)?.let(WalkingCheckpointJsonCodec::decode)
    override fun clear(walkId: String) { prefs.edit().remove(walkId).apply() }
    companion object { private const val PREFS = "walking_state_v1" }
}
