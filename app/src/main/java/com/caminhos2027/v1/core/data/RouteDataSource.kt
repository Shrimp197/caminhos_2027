package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Route

/** Source of official route reference data. Implementations must not invent missing data. */
interface RouteDataSource {
    fun loadRoute(): Route
}

/** Repository implementation that validates route data before exposing it to the app. */
class ValidatingRouteRepository(
    private val dataSource: RouteDataSource
) : RouteRepository {
    override fun getRoute(routeId: String): Result<Route> = runCatching {
        val route = RouteValidator.requireValid(dataSource.loadRoute())
        require(route.id == routeId) { "Route id mismatch: expected $routeId, got ${route.id}" }
        route
    }
}
