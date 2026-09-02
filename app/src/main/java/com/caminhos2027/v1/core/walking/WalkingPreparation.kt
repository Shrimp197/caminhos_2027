package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk

/** Read model used by the preparation experience. Official stages remain reference data. */
data class WalkingPreparation(
    val route: Route,
    val walk: Walk,
    val stages: List<Stage>,
    val relevantApoi: List<Apoi>
)

object WalkingPreparationBuilder {
    fun build(route: Route, walk: Walk, publishedApoi: List<Apoi>): WalkingPreparation {
        require(walk.routeId == route.id) { "Walk and route must match" }

        val start = walk.plannedStartKm ?: 0.0
        val destination = walk.plannedDestinationKm ?: route.totalDistanceKm
        require(start in 0.0..route.totalDistanceKm) { "Planned start must be within route bounds" }
        require(destination in 0.0..route.totalDistanceKm) { "Planned destination must be within route bounds" }
        require(destination > start) { "Planned destination must be after start" }

        val stages = route.stages
            .filter { it.id in walk.stageIds }
            .sortedBy { it.startRouteKm }

        val apoi = publishedApoi
            .asSequence()
            .filter { it.location.routeId == route.id }
            .filter { it.publication.status == PublicationStatus.PUBLISHED || it.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING }
            .filter { it.location.routeKm != null }
            .filter { it.location.routeKm!! >= start && it.location.routeKm <= destination }
            .sortedBy { it.location.routeKm }
            .toList()

        return WalkingPreparation(route, walk, stages, apoi)
    }
}
