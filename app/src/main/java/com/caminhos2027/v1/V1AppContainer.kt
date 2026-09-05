package com.caminhos2027.v1

import android.content.Context
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.data.AssetApoiDataSource
import com.caminhos2027.v1.core.data.AssetRouteDataSource
import com.caminhos2027.v1.core.data.RouteJsonMetadata
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
 * construction of the route, published APOI catalog and persistent walking runtime.
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
            routeAssetPath: String = "data/route.geojson",
            apoiAssetPath: String = "data/published/apoi-production.json"
        ): V1AppContainer {
            val route = AssetRouteDataSource(
                context = context,
                assetPath = routeAssetPath,
                metadata = RouteJsonMetadata(
                    officialDistanceKm = 211.87,
                    officialName = "Caminho do Centenário"
                )
            ).loadRoute()
            val catalog = PublishedApoiCatalog(
                ApoiRepository(AssetApoiDataSource(context, apoiAssetPath))
            )
            val walkRepository = AndroidWalkRepository(context)
            val checkpointRepository = AndroidWalkingStateRepository(context)
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
