package com.caminhos2027.v1.core

import android.content.Context
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.data.AssetApoiDataSource
import com.caminhos2027.v1.core.data.AssetRouteDataSource
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.AndroidWalkRepository
import com.caminhos2027.v1.core.walking.AndroidWalkingStateRepository
import com.caminhos2027.v1.core.walking.WalkingAppStateController
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import com.caminhos2027.v1.core.walking.WalkingSessionService

/** Android composition boundary for the V1 walking read model and persistent session runtime. */
class AndroidV1AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val route = AssetRouteDataSource(appContext, "data/route.geojson").loadRoute()
    private val catalog = PublishedApoiCatalog(ApoiRepository(AssetApoiDataSource(appContext)))
    private val walkRepository = AndroidWalkRepository(appContext)
    private val walkingStateRepository = AndroidWalkingStateRepository(appContext)
    private val sessionService = WalkingSessionService(walkRepository, walkingStateRepository)

    val store = AppStateStore()
    val runtime = WalkingSessionRuntime(route, sessionService, catalog.all())

    fun controller(walk: Walk): WalkingAppStateController =
        WalkingAppStateController(route, walk, catalog, store, runtime)

    fun currentRoutePosition(): RoutePosition? = store.state.walking?.routePosition

    fun acceptGps(position: RawGpsPosition): AppState {
        val walking = checkNotNull(store.state.walking) { "No active walking session" }
        return controller(walking.walk).acceptGps(position)
    }
}
