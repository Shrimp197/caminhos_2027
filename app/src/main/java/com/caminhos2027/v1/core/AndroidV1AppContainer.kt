package com.caminhos2027.v1.core

import android.content.Context
import com.caminhos2027.v1.V1AppContainer
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.WalkingAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationService

/** Android composition boundary for V1 walking preparation and the persistent session read model. */
class AndroidV1AppContainer(context: Context) {
    private val base = V1AppContainer.forAndroid(context)
    private val preparationService = WalkingPreparationService(
        base.route,
        // The published catalog is already sourced from the production asset by the shared root.
        base.catalog
    )

    val store: AppStateStore = base.appStateStore
    val runtime = base.sessionRuntime
    val preparationController = WalkingPreparationAppStateController(
        route = base.route,
        preparationService = preparationService,
        store = store,
        sessionRuntime = runtime
    )

    private var controller: WalkingAppStateController? = null

    fun attachWalk(walk: Walk): WalkingAppStateController {
        require(walk.routeId == base.route.id) { "Walk route must match the published V1 route" }
        val existing = controller
        if (existing != null && store.state.walking?.walk?.id == walk.id) return existing
        return base.controller(walk).also { controller = it }
    }

    fun activeController(): WalkingAppStateController =
        requireNotNull(controller) { "No V1 walking session is attached" }

    fun publishedRoute() = base.route
    fun publishedApoiCatalog(): PublishedApoiCatalog = base.catalog

    fun clearSession() {
        controller = null
        store.setWalking(null)
    }
}
