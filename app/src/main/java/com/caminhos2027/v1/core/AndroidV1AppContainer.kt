package com.caminhos2027.v1.core

import android.content.Context
import com.caminhos2027.v1.V1AppContainer
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.WalkingApoiDecisionController
import com.caminhos2027.v1.core.walking.WalkingAppStateController
import com.caminhos2027.v1.core.walking.WalkingPreparationAppStateController
import com.caminhos2027.v1.core.walking.WalkingSessionAttachmentPolicy
import com.caminhos2027.v1.core.walking.WalkingSessionRuntime
import java.time.Instant

/** Android composition boundary for V1 walking preparation, consultation and the persistent session read model. */
class AndroidV1AppContainer internal constructor(
    private val base: V1AppContainer
) {
    constructor(context: Context) : this(V1AppContainer.forAndroid(context.applicationContext))

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

    /**
     * Reuses the controller during repeated UI attachment, while preventing cross-session controller replacement.
     * The persistent active walk is checked separately from the read model so clearSession() cannot erase this guard.
     */
    fun attachWalk(walk: Walk): WalkingAppStateController {
        WalkingSessionAttachmentPolicy.requireAttachable(
            publishedRoute = base.route,
            requestedWalk = walk,
            attachedWalkId = attachedWalkId,
            existingController = controller != null,
            publishedStateWalk = store.state.walking?.walk,
            persistentActiveWalk = runtime.activeWalk()
        )

        val existing = controller
        if (existing != null && attachedWalkId == walk.id) return existing

        return base.controller(walk).also {
            controller = it
            attachedWalkId = walk.id
        }
    }

    /**
     * Recreates the attached controller from the persistent active session after Android process/UI recreation.
     * Runtime restoration is completed before the restored state is published into the new composition boundary.
     */
    fun resumePersistedWalk(now: Instant = Instant.now()): AppState {
        val persisted = runtime.activeWalk()
        if (persisted == null) {
            clearSession()
            return store.state
        }

        WalkingSessionAttachmentPolicy.requireAttachable(
            publishedRoute = base.route,
            requestedWalk = persisted,
            attachedWalkId = attachedWalkId,
            existingController = controller != null,
            publishedStateWalk = store.state.walking?.walk,
            persistentActiveWalk = persisted
        )

        val restored = runtime.resume(now)
        if (restored == null) {
            clearSession()
            return store.state
        }

        val existing = controller
        if (existing == null || attachedWalkId != restored.walk.id) {
            controller = base.controller(restored.walk)
            attachedWalkId = restored.walk.id
        }
        store.setWalking(restored)
        return store.state
    }

    fun activeController(): WalkingAppStateController =
        requireNotNull(controller) { "No V1 walking session is attached" }

    fun publishedRoute() = base.route
    fun publishedApoiCatalog() = base.catalog

    /** Clears only the composition/read-model attachment; persistent walking lifecycle remains owned by runtime. */
    fun clearSession() {
        controller = null
        attachedWalkId = null
        store.setWalking(null)
        store.clearApoiBrowser()
        store.clearDecision()
    }
}
