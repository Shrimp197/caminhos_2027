package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import java.time.Instant

/** Publishes preparation into shared V1 state and starts the saved walk explicitly. */
class WalkingPreparationAppStateController(
    private val route: Route,
    private val preparationService: WalkingPreparationService,
    private val catalog: PublishedApoiCatalog,
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

    /** Starts only the walk that was previously saved into the shared AppState. */
    fun startSaved(position: RoutePosition, now: Instant = Instant.now()): AppState {
        val walking = requireNotNull(store.state.walking) { "No prepared walk in AppState" }
        val controller = WalkingAppStateController(
            route = route,
            walk = walking.walk,
            catalog = catalog,
            store = store
        )
        return controller.start(position, now)
    }
}
