package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus

/**
 * Pure attachment rules for the Android composition boundary.
 *
 * UI recreation may attach the same walk repeatedly, but an already-attached active
 * session must never be silently replaced by another walk. The policy is deliberately
 * independent from Android so the lifecycle boundary can be regression-tested cheaply.
 */
object WalkingSessionAttachmentPolicy {
    fun requireAttachable(
        publishedRoute: Route,
        requestedWalk: Walk,
        attachedWalkId: String?,
        existingController: Boolean,
        publishedStateWalk: Walk?,
        persistentActiveWalk: Walk? = null
    ) {
        require(requestedWalk.routeId == publishedRoute.id) {
            "Walk route must match the published V1 route"
        }

        if (!existingController || attachedWalkId == null || attachedWalkId == requestedWalk.id) return

        require(persistentActiveWalk?.status != WalkStatus.ACTIVE) {
            "Cannot replace an active V1 walking session with a different walk"
        }
        require(publishedStateWalk?.status != WalkStatus.ACTIVE) {
            "Cannot replace an active V1 walking session with a different walk"
        }
    }
}
