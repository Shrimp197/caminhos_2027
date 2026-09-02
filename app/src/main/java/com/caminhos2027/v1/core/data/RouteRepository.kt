package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Route

/** Source of official route reference data. UI and walking logic depend on this abstraction, not JSON files. */
interface RouteRepository {
    fun getRoute(routeId: String): Result<Route>
}
