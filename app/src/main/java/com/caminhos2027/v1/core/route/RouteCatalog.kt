package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.Route

/** Small application-facing catalog; it keeps route selection independent from UI and file format. */
class RouteCatalog(
    private val repository: RouteRepository
) {
    fun requireRoute(id: String): Route =
        requireNotNull(repository.getById(id)) { "Route not found: $id" }
}
