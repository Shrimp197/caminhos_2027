package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Route

/** Source of official route reference data. Implementations must not invent missing data. */
interface RouteDataSource {
    fun loadRoute(): Route
}

/** Repository boundary used by the rest of the app. Validation happens before publication. */
class RouteRepository(
    private val dataSource: RouteDataSource
) {
    fun getRoute(): Route = RouteValidator.requireValid(dataSource.loadRoute())
}
