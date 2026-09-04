package com.caminhos2027.v1.core

import android.content.Context
import com.caminhos2027.v1.V1AppContainer
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
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
    private var attachedWalkId: String? = null

    /** Reuses the controller during repeated UI attachment, while preventing cross-session controller replacement. */
    fun attachWalk(walk: Walk): WalkingAppStateController {
        require(walk.routeId == base.route.id) { "Walk route must match the published V1 route" }
        val existing = controller
        if (existing != null && attachedWalkId == walk.id) return existing

        val publishedWalk = store.state.walking?.walk
        require(
            existing == null ||
                attachedWalkId == null ||
                (attachedWalkId == walk.id) ||
                publishedWalk?.status != WalkStatus.ACTIVE
        ) {
            "Cannot replace an active V1 walking session with a different walk"
        }

        return base.controller(walk).also {
            controller = it
            attachedWalkId = walk.id
        }
    }

    fun activeController(): WalkingAppStateController =
        requireNotNull(controller) { "No V1 walking session is attached" }

    fun publishedRoute() = base.route
    fun publishedApoiCatalog() = base.catalog

    fun clearSession() {
        controller = null
        attachedWalkId = null
        store.setWalking(null)
        store.clearApoiBrowser()
        store.clearDecision()
    }
}
