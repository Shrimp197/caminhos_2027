package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.Route

/** Publishes the saved preparation into the shared V1 state before walking starts. */
class WalkingPreparationAppStateController(
    private val route: Route,
    private val preparationService: WalkingPreparationService,
    private val store: AppStateStore
) {
    fun preview(walkId: String, startRouteKm: Double, destinationRouteKm: Double): WalkingPreparation =
        preparationService.preview(walkId, startRouteKm, destinationRouteKm)

    fun save(walkId: String, startRouteKm: Double, destinationRouteKm: Double): AppState {
        val preparation = preparationService.save(walkId, startRouteKm, destinationRouteKm)
        store.setWalking(
            WalkingStateBuilder.build(
                route = route,
                walk = preparation.walk,
                gpsState = com.caminhos2027.v1.core.route.GpsState.NO_SIGNAL,
                routePosition = null,
                publishedApoi = preparation.relevantApoi
            )
        )
        return store.state
    }
}
