package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk

/** Creates a planned walking session without changing official route or stage data. */
object WalkingPlanFactory {
    fun create(
        route: Route,
        walkId: String,
        startRouteKm: Double,
        destinationRouteKm: Double
    ): Walk {
        require(walkId.isNotBlank()) { "Walk id is required" }
        require(startRouteKm in 0.0..route.totalDistanceKm) {
            "Start position must be within route bounds"
        }
        require(destinationRouteKm in 0.0..route.totalDistanceKm) {
            "Destination position must be within route bounds"
        }
        require(destinationRouteKm > startRouteKm) {
            "Destination must be after the start position"
        }

        val stageIds = route.stages
            .filter { stage ->
                stage.routeId == route.id &&
                    stage.endRouteKm > startRouteKm &&
                    stage.startRouteKm < destinationRouteKm
            }
            .sortedBy { it.startRouteKm }
            .map { it.id }

        return Walk(
            id = walkId,
            routeId = route.id,
            plannedStartKm = startRouteKm,
            plannedDestinationKm = destinationRouteKm,
            stageIds = stageIds
        )
    }
}
