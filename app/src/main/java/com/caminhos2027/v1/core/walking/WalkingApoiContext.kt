package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.Apoi

/**
 * Application-facing APOI context for an active walk.
 * Keeps discovery anchored to the current walking position and published catalog.
 */
class WalkingApoiContext(
    private val routeId: String,
    private val catalog: PublishedApoiCatalog
) {
    fun ahead(currentRouteKm: Double, limit: Int = 8): List<ApoiAhead> =
        catalog.ahead(routeId, currentRouteKm, limit)

    fun searchAhead(
        currentRouteKm: Double,
        query: String,
        filter: ApoiFilter = ApoiFilter(),
        limit: Int = 8
    ): List<ApoiAhead> = catalog.ahead(routeId, currentRouteKm, query, filter, limit)

    fun next(currentRouteKm: Double): Apoi? = catalog.next(routeId, currentRouteKm)
}
