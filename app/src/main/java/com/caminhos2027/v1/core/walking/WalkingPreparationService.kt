package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkStatus

/**
 * Application service for preparing a walk.
 * APOI come from the published catalog; the preparation layer never reads JSON directly.
 */
class WalkingPreparationService(
    private val route: Route,
    private val walkRepository: WalkRepository,
    private val apoiCatalog: PublishedApoiCatalog
) {
    fun preview(
        walkId: String,
        startRouteKm: Double,
        destinationRouteKm: Double
    ): WalkingPreparation {
        val walk = WalkingPlanFactory.create(route, walkId, startRouteKm, destinationRouteKm)
        return WalkingPreparationBuilder.build(route, walk, relevantApoi(walk))
    }

    fun save(
        walkId: String,
        startRouteKm: Double,
        destinationRouteKm: Double
    ): WalkingPreparation {
        val preparation = preview(walkId, startRouteKm, destinationRouteKm)
        walkRepository.save(preparation.walk)
        return preparation
    }

    /** Restores the most recently stored planned walk without starting it. */
    fun restorePlanned(): WalkingPreparation? =
        walkRepository.list()
            .asReversed()
            .firstOrNull { it.status == WalkStatus.PLANNED && it.routeId == route.id }
            ?.let { walk ->
                WalkingPreparationBuilder.build(route, walk, relevantApoi(walk))
            }

    private fun relevantApoi(walk: com.caminhos2027.v1.core.model.Walk) =
        apoiCatalog.filter(
            routeId = route.id,
            currentRouteKm = walk.plannedStartKm ?: 0.0
        ).filter { apoi ->
            val km = apoi.location.routeKm ?: return@filter false
            km <= (walk.plannedDestinationKm ?: route.totalDistanceKm)
        }
}
