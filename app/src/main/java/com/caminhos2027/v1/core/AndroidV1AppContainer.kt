package com.caminhos2027.v1.core

import android.content.Context
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.data.AssetApoiDataSource
import com.caminhos2027.v1.core.data.AssetRouteDataSource
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.AndroidWalkRepository
import com.caminhos2027.v1.core.walking.AndroidWalkingStateRepository
import com.caminhos2027.v1.core.walking.WalkingAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationService
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import com.caminhos2027.v1.core.walking.WalkingSessionService

/** Android composition boundary for V1 walking preparation and the persistent session read model. */
class AndroidV1AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val route = AssetRouteDataSource(appContext, "data/route.geojson").loadRoute()
    private val catalog = PublishedApoiCatalog(ApoiRepository(AssetApoiDataSource(appContext)))
    private val walkRepository = AndroidWalkRepository(appContext)
    private val walkingStateRepository = AndroidWalkingStateRepository(appContext)
    private val sessionService = WalkingSessionService(walkRepository, walkingStateRepository)
    private val preparationService = WalkingPreparationService(route, walkRepository, catalog)

    val store = AppStateStore()
    val runtime = WalkingSessionRuntime(route, sessionService, catalog.all())
    val preparationController = WalkingPreparationAppStateController(
        route = route,
        preparationService = preparationService,
        store = store,
        sessionRuntime = runtime
    )

    private var controller: WalkingAppStateController? = null

    fun attachWalk(walk: Walk): WalkingAppStateController {
        require(walk.routeId == route.id) { "Walk route must match the published V1 route" }
        val existing = controller
        if (existing != null && store.state.walking?.walk?.id == walk.id) return existing
        return WalkingAppStateController(route, walk, catalog, store, runtime).also { controller = it }
    }

    fun activeController(): WalkingAppStateController =
        requireNotNull(controller) { "No V1 walking session is attached" }

    fun publishedRoute() = route
    fun publishedApoiCatalog() = catalog

    fun clearSession() {
        controller = null
        store.setWalking(null)
    }
}
