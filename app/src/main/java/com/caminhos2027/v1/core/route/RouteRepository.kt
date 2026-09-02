package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Route

/** Boundary between the walking domain and versioned route datasets. */
interface RouteRepository {
    fun getById(id: String): Route?
}

/** Deterministic repository for tests and early integration. Production will use the published dataset. */
class InMemoryRouteRepository(routes: List<Route>) : RouteRepository {
    private val routesById = routes.associateBy { it.id }

    override fun getById(id: String): Route? = routesById[id]
}
