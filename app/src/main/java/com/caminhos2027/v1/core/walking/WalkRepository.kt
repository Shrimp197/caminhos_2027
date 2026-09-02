package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Walk

/** Persistence boundary for walking sessions. Android/Room implementations can be added later. */
interface WalkRepository {
    fun save(walk: Walk)
    fun getById(id: String): Walk?
    fun getActive(): Walk?
    fun list(): List<Walk>
}

/** Deterministic repository used by domain tests and early integration work. */
class InMemoryWalkRepository : WalkRepository {
    private val walks = linkedMapOf<String, Walk>()

    override fun save(walk: Walk) {
        walks[walk.id] = walk
    }

    override fun getById(id: String): Walk? = walks[id]

    override fun getActive(): Walk? = walks.values.firstOrNull { it.status == com.caminhos2027.v1.core.model.WalkStatus.ACTIVE }

    override fun list(): List<Walk> = walks.values.toList()
}
