package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Route

/** Repository that exposes only datasets that pass the structural route gate. */
class ValidatedRouteRepository(
    routes: List<Route>
) : RouteRepository {
    private val validRoutes: Map<String, Route> = routes.mapNotNull { route ->
        route.takeIf { RouteDatasetValidator.validate(it).valid }
    }.associateBy { it.id }

    override fun getById(id: String): Route? = validRoutes[id]
}
