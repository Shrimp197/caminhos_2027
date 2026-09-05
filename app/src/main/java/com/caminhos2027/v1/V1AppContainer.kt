package com.caminhos2027.v1

import android.content.Context
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.data.AssetApoiDataSource
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.AndroidWalkRepository
import com.caminhos2027.v1.core.walking.AndroidWalkingStateRepository
import com.caminhos2027.v1.core.walking.WalkingAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationService
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import com.caminhos2027.v1.core.walking.WalkingSessionService

/**
 * Android composition boundary for V1.
 *
 * The launcher/activity owns lifecycle and presentation only; this object owns the
 * construction of the selected route, published APOI catalog and persistent walking runtime.
 */
class V1AppContainer(
    val route: Route,
    val catalog: PublishedApoiCatalog,
    val appStateStore: AppStateStore,
    val sessionRuntime: WalkingSessionRuntime,
    val preparationService: WalkingPreparationService? = null
) {
    fun controller(walk: Walk): WalkingAppStateController =
        WalkingAppStateController(
            route = route,
            walk = walk,
            catalog = catalog,
            store = appStateStore,
            sessionRuntime = sessionRuntime
        )

    companion object {
        fun forAndroid(
            context: Context,
            routeId: String? = null,
            apoiAssetPath: String = "data/published/apoi-production.json"
        ): V1AppContainer {
            val applicationContext = context.applicationContext
            val selectedRouteId = routeId
                ?: AndroidRouteCatalog.preferredPersistedRouteId(applicationContext)
                ?: AndroidRouteCatalog.CENTENARIO_ID
            val route = AndroidRouteCatalog.loadRoute(applicationContext, selectedRouteId)
            val catalog = PublishedApoiCatalog(
                ApoiRepository(AssetApoiDataSource(applicationContext, apoiAssetPath))
            )
            val walkRepository = AndroidWalkRepository(applicationContext)
            val checkpointRepository = AndroidWalkingStateRepository(applicationContext)
            val sessionService = WalkingSessionService(walkRepository, checkpointRepository)
            val sessionRuntime = WalkingSessionRuntime(route, sessionService, catalog.all())
            val preparationService = WalkingPreparationService(route, walkRepository, catalog)
            return V1AppContainer(
                route = route,
                catalog = catalog,
                appStateStore = AppStateStore(),
                sessionRuntime = sessionRuntime,
                preparationService = preparationService
            )
        }
    }
}
