package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.Apoi

/**
 * Application-facing APOI catalog.
 * It consumes only the repository's already-published dataset and never re-qualifies it.
 */
class PublishedApoiCatalog(
    private val repository: ApoiRepository
) {
    fun all(): List<Apoi> = repository.getAll()

    fun filter(
        routeId: String,
        currentRouteKm: Double,
        filter: ApoiFilter = ApoiFilter()
    ): List<Apoi> = ApoiFilterEngine.apply(all(), routeId, currentRouteKm, filter)

    fun search(query: String): List<Apoi> = ApoiSearch.apply(all(), query)

    fun searchAhead(
        routeId: String,
        currentRouteKm: Double,
        query: String,
        filter: ApoiFilter = ApoiFilter()
    ): List<Apoi> = ApoiSearch.apply(filter(routeId, currentRouteKm, filter), query)

    fun ahead(
        routeId: String,
        currentRouteKm: Double,
        limit: Int = 8
    ): List<ApoiAhead> = NextApoiList.findAhead(all(), routeId, currentRouteKm, limit)

    fun ahead(
        routeId: String,
        currentRouteKm: Double,
        query: String,
        filter: ApoiFilter = ApoiFilter(),
        limit: Int = 8
    ): List<ApoiAhead> = NextApoiList.findAhead(
        ApoiSearch.apply(filter(routeId, currentRouteKm, filter), query),
        routeId,
        currentRouteKm,
        limit
    )

    fun next(routeId: String, currentRouteKm: Double): Apoi? =
        NextApoiFinder.findNext(all(), routeId, currentRouteKm)
}
