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
    store: AppStateStore = AppStateStore(),
    private val sessionRuntime: WalkingSessionRuntime? = null
) {
    private val publishedApoi = catalog.all()
    private val appStateStore = store
    private var currentWalk = walk
    private var coordinator = WalkingStateCoordinator(route, currentWalk, publishedApoi)

    init {
        appStateStore.setWalking(coordinator.state)
    }

    /** Starts through the persistent runtime when configured, otherwise uses the in-memory coordinator. */
    fun start(position: RoutePosition, now: Instant = Instant.now()): AppState {
        val runtime = sessionRuntime
        if (runtime != null) {
            runtime.prepare(currentWalk)
            val walking = runtime.start(currentWalk.id, position, now)
            currentWalk = walking.walk
            coordinator = WalkingStateCoordinator(route, currentWalk, publishedApoi)
            return publishState(walking)
        }

        currentWalk = WalkingSessionController.start(currentWalk, position, now)
        coordinator = WalkingStateCoordinator(route, currentWalk, publishedApoi)
        return publish { coordinator.seedStartPosition(position, now) }
    }

    /** Restores an active walk from the persistent runtime into the shared application read model. */
    fun resume(now: Instant = Instant.now()): AppState {
        val runtime = requireNotNull(sessionRuntime) {
            "A persistent walking session runtime is required to resume"
        }
        val walking = runtime.resume(now)
        if (walking == null) {
            appStateStore.setWalking(null)
            return appStateStore.state
        }
        currentWalk = walking.walk
        return publishState(walking)
    }

    /** Compatibility entry point for tests/consumers that already own the lifecycle transition. */
    fun seedStartPosition(position: RoutePosition): AppState =
        publish { coordinator.seedStartPosition(position) }

    fun acceptGps(position: RawGpsPosition): AppState {
        val runtime = sessionRuntime
        if (runtime != null) return publishState(runtime.accept(position))
        return publish { coordinator.accept(position) }
    }

    fun markNoSignal(now: Instant): AppState {
        val runtime = sessionRuntime
        if (runtime != null) return publishState(runtime.markNoSignal(now))
        return publish { coordinator.markNoSignal(now) }
    }

    fun setOffline(offline: Boolean): AppState {
        val runtime = sessionRuntime
        if (runtime != null) return publishState(runtime.setOffline(offline))
        return publish { coordinator.setOffline(offline) }
    }

    private fun publishState(walking: WalkingState): AppState {
        appStateStore.setWalking(walking)
        return appStateStore.state
    }

    private fun publish(update: () -> WalkingState): AppState {
        appStateStore.setWalking(update())
        return appStateStore.state
    }
}
