package com.caminhos2027.v1.core

import android.content.Context
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.data.AssetApoiDataSource
import com.caminhos2027.v1.core.data.AssetRouteDataSource
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.AndroidWalkRepository
import com.caminhos2027.v1.core.walking.AndroidWalkingStateRepository
import com.caminhos2027.v1.core.walking.WalkingApoiDecisionController
import com.caminhos2027.v1.core.walking.WalkingAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationAppStateController
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime

/** Android composition boundary for V1 walking preparation, consultation and the persistent session read model. */
class AndroidV1AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val base = V1AppContainer.forAndroid(appContext)

    val store: AppStateStore = base.appStateStore
    val runtime: WalkingSessionRuntime = base.sessionRuntime
    val preparationController = WalkingPreparationAppStateController(
        route = base.route,
        preparationService = requireNotNull(base.preparationService) { "Android V1 preparation service is not configured" },
        store = store,
        sessionRuntime = runtime
    )
    val apoiDecisionController = WalkingApoiDecisionController(
        route = base.route,
        catalog = base.catalog,
        store = store
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
    fun publishedApoiCatalog() = base.catalog

    fun clearSession() {
        controller = null
        store.setWalking(null)
        store.clearApoiSelection(com.caminhos2027.v1.core.apoi.ApoiBrowser(base.catalog))
        store.clearDecision()
    }
}
