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

    fun next(routeId: String, currentRouteKm: Double): Apoi? =
        NextApoiFinder.findNext(all(), routeId, currentRouteKm)
}
