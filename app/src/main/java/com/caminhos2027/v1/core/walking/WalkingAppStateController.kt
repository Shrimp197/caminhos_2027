package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import java.time.Instant

/** Bridges the walking lifecycle and coordinator into the shared application read model. */
class WalkingAppStateController(
    private val route: Route,
    walk: Walk,
    catalog: PublishedApoiCatalog,
    store: AppStateStore = AppStateStore()
) {
    private val publishedApoi = catalog.all()
    private val appStateStore = store
    private var currentWalk = walk
    private var coordinator = WalkingStateCoordinator(route, currentWalk, publishedApoi)

    init {
        appStateStore.setWalking(coordinator.state)
    }

    /** Starts the domain walk first, then publishes its first real route position. */
    fun start(position: RoutePosition, now: Instant = Instant.now()): AppState {
        currentWalk = WalkingSessionController.start(currentWalk, position, now)
        coordinator = WalkingStateCoordinator(route, currentWalk, publishedApoi)
        return publish { coordinator.seedStartPosition(position, now) }
    }

    /** Compatibility entry point for tests/consumers that already own the lifecycle transition. */
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
