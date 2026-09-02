package com.caminhos2027.v1.core.walking

/** Persistence boundary for the transient/current walking read state.
 *
 * The walk lifecycle remains persisted through WalkRepository. RoutePosition is kept separate
 * because it represents the latest device-derived position, not a change to the official walk plan.
 */
interface WalkingStateRepository {
    fun save(state: WalkingState)
    fun get(walkId: String): WalkingState?
    fun clear(walkId: String)
}

/** Deterministic implementation for early integration and domain tests. */
class InMemoryWalkingStateRepository : WalkingStateRepository {
    private val states = linkedMapOf<String, WalkingState>()

    override fun save(state: WalkingState) {
        states[state.walk.id] = state
    }

    override fun get(walkId: String): WalkingState? = states[walkId]

    override fun clear(walkId: String) {
        states.remove(walkId)
    }
}
