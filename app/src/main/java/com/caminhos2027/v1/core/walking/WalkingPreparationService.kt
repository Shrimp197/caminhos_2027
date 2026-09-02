package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route

/** Coordinates preparation without inventing automatic choices for the pilgrim. */
class WalkingPreparationService(
    private val route: Route,
    private val walkRepository: WalkRepository,
    private val publishedApoi: List<Apoi> = emptyList()
) {
    fun preview(walkId: String, startRouteKm: Double, destinationRouteKm: Double): WalkingPreparation {
        val walk = WalkingPlanFactory.create(route, walkId, startRouteKm, destinationRouteKm)
        return WalkingPreparationBuilder.build(route, walk, publishedApoi)
    }

    fun save(walkId: String, startRouteKm: Double, destinationRouteKm: Double): WalkingPreparation {
        val preparation = preview(walkId, startRouteKm, destinationRouteKm)
        walkRepository.save(preparation.walk)
        return preparation
    }
}
