package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.Apoi

/**
 * Application-facing catalog backed only by the already-qualified publication dataset.
 * Qualification belongs to the publication pipeline; the app must not reinterpret master data.
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
