package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk

/**
 * Presents the consequences of stopping now or continuing to the planned destination.
 * It deliberately does not recommend either option.
 */
data class WalkingDecisionOption(
    val id: String,
    val title: String,
    val distanceKm: Double,
    val relevantApoi: List<Apoi>
)

data class WalkingDecisionContext(
    val currentRouteKm: Double,
    val remainingToPlannedDestinationKm: Double,
    val stopNow: WalkingDecisionOption,
    val continueWalking: WalkingDecisionOption
)

object WalkingDecisionSupport {
    fun build(
        route: Route,
        walk: Walk,
        position: RoutePosition,
        publishedApoi: List<Apoi>
    ): WalkingDecisionContext {
        require(walk.routeId == route.id) { "Walk and route must match" }
        require(position.routeId == route.id) { "Position and route must match" }
        val destination = walk.plannedDestinationKm ?: route.totalDistanceKm
        require(destination in 0.0..route.totalDistanceKm) { "Destination must be within route bounds" }
        require(position.routeKm in 0.0..route.totalDistanceKm) { "Position must be within route bounds" }

        val remaining = (destination - position.routeKm).coerceAtLeast(0.0)
        val ahead = publishedApoi
            .asSequence()
            .filter { it.location.routeId == route.id }
            .mapNotNull { apoi -> apoi.location.routeKm?.let { it to apoi } }
            .filter { (km, _) -> km >= position.routeKm && km <= destination }
            .sortedBy { it.first }
            .map { it.second }
            .toList()

        return WalkingDecisionContext(
            currentRouteKm = position.routeKm,
            remainingToPlannedDestinationKm = remaining,
            stopNow = WalkingDecisionOption(
                id = "stop-now",
                title = "Parar agora",
                distanceKm = 0.0,
                relevantApoi = emptyList()
            ),
            continueWalking = WalkingDecisionOption(
                id = "continue-to-planned-destination",
                title = "Continuar até ao destino planeado",
                distanceKm = remaining,
                relevantApoi = ahead
            )
        )
    }
}
