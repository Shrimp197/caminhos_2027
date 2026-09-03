package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import java.time.Instant

/** Bridges the walking domain coordinator into the shared application read model. */
class WalkingAppStateController(
    route: Route,
    walk: Walk,
    catalog: PublishedApoiCatalog,
    store: AppStateStore = AppStateStore()
) {
    private val coordinator = WalkingStateCoordinator(route, walk, catalog.all())
    private val appStateStore = store

    init {
        appStateStore.setWalking(coordinator.state)
    }

    fun seedStartPosition(position: RoutePosition): AppState =
        publish { coordinator.seedStartPosition(position) }

    fun acceptGps(position: RawGpsPosition): AppState =
        publish { coordinator.accept(position) }

    fun markNoSignal(now: Instant): AppState =
        publish { coordinator.markNoSignal(now) }

    fun setOffline(offline: Boolean): AppState =
        publish { coordinator.setOffline(offline) }

    private fun publish(update: () -> WalkingState): AppState {
        appStateStore.setWalking(update())
        return appStateStore.state
    }
}
