package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure lifecycle-boundary regressions for repeated UI attachment and session isolation. */
class WalkingSessionAttachmentPolicyTest {
    private val route = route("published")
    private val foreignRoute = route("foreign")

    @Test
    fun firstAttachmentIsAllowed() {
        requireAttachable(
            requestedWalk = walk("walk-1", route.id, WalkStatus.PLANNED),
            attachedWalkId = null,
            existingController = false,
            publishedStateWalk = null
        )
    }

    @Test
    fun sameWalkCanBeReattachedEvenWhenActive() {
        requireAttachable(
            requestedWalk = walk("walk-1", route.id, WalkStatus.ACTIVE),
            attachedWalkId = "walk-1",
            existingController = true,
            publishedStateWalk = walk("walk-1", route.id, WalkStatus.ACTIVE),
            persistentActiveWalk = walk("walk-1", route.id, WalkStatus.ACTIVE)
        )
    }

    @Test
    fun differentWalkCanReplaceWhenPreviousPublishedStateIsNotActive() {
        requireAttachable(
            requestedWalk = walk("walk-2", route.id, WalkStatus.PLANNED),
            attachedWalkId = "walk-1",
            existingController = true,
            publishedStateWalk = walk("walk-1", route.id, WalkStatus.COMPLETED)
        )
    }

    @Test
    fun differentWalkCanAttachWhenStoreHasAlreadyBeenClearedAndNoPersistentWalkIsActive() {
        requireAttachable(
            requestedWalk = walk("walk-2", route.id, WalkStatus.PLANNED),
            attachedWalkId = "walk-1",
            existingController = true,
            publishedStateWalk = null,
            persistentActiveWalk = null
        )
    }

    @Test
    fun persistentActiveWalkStillBlocksReplacementAfterReadModelWasCleared() {
        val activeBefore = walk("walk-1", route.id, WalkStatus.ACTIVE)
        val error = assertFails {
            WalkingSessionAttachmentPolicy.requireAttachable(
                publishedRoute = route,
                requestedWalk = walk("walk-2", route.id, WalkStatus.PLANNED),
                attachedWalkId = "walk-1",
                existingController = true,
                publishedStateWalk = null,
                persistentActiveWalk = activeBefore
            )
        }

        assertTrue(error.message.orEmpty().contains("active V1 walking session"))
        assertTrue(activeBefore.status == WalkStatus.ACTIVE)
        assertTrue(activeBefore.id == "walk-1")
    }

    @Test
    fun activeDifferentWalkIsRejectedWithoutMutatingAnything() {
        val before = walk("walk-1", route.id, WalkStatus.ACTIVE)
        val error = assertFails {
            WalkingSessionAttachmentPolicy.requireAttachable(
                publishedRoute = route,
                requestedWalk = walk("walk-2", route.id, WalkStatus.PLANNED),
                attachedWalkId = "walk-1",
                existingController = true,
                publishedStateWalk = before
            )
        }

        assertTrue(error.message.orEmpty().contains("active V1 walking session"))
        assertTrue(before.status == WalkStatus.ACTIVE)
        assertTrue(before.id == "walk-1")
    }

    @Test
    fun foreignRouteIsRejectedBeforeAttachmentRegardlessOfExistingState() {
        val error = assertFails {
            WalkingSessionAttachmentPolicy.requireAttachable(
                publishedRoute = route,
                requestedWalk = walk("foreign-walk", foreignRoute.id, WalkStatus.PLANNED),
                attachedWalkId = null,
                existingController = false,
                publishedStateWalk = null
            )
        }

        assertTrue(error.message.orEmpty().contains("published V1 route"))
    }

    @Test
    fun foreignRouteIsRejectedEvenIfTheWalkIdLooksLikeTheAttachedSession() {
        val error = assertFails {
            WalkingSessionAttachmentPolicy.requireAttachable(
                publishedRoute = route,
                requestedWalk = walk("walk-1", foreignRoute.id, WalkStatus.ACTIVE),
                attachedWalkId = "walk-1",
                existingController = true,
                publishedStateWalk = walk("walk-1", route.id, WalkStatus.ACTIVE)
            )
        }

        assertTrue(error.message.orEmpty().contains("published V1 route"))
    }

    @Test
    fun staleAttachedIdDoesNotBlockFirstControllerCreation() {
        requireAttachable(
            requestedWalk = walk("walk-2", route.id, WalkStatus.PLANNED),
            attachedWalkId = "stale-id",
            existingController = false,
            publishedStateWalk = walk("walk-1", route.id, WalkStatus.ACTIVE),
            persistentActiveWalk = walk("walk-1", route.id, WalkStatus.ACTIVE)
        )
    }

    private fun requireAttachable(
        requestedWalk: Walk,
        attachedWalkId: String?,
        existingController: Boolean,
        publishedStateWalk: Walk?,
        persistentActiveWalk: Walk? = null
    ) {
        WalkingSessionAttachmentPolicy.requireAttachable(
            publishedRoute = route,
            requestedWalk = requestedWalk,
            attachedWalkId = attachedWalkId,
            existingController = existingController,
            publishedStateWalk = publishedStateWalk,
            persistentActiveWalk = persistentActiveWalk
        )
    }

    private fun assertFails(block: () -> Unit): IllegalArgumentException = try {
        block()
        throw AssertionError("Expected attachment to be rejected")
    } catch (error: IllegalArgumentException) {
        error
    }

    private fun walk(id: String, routeId: String, status: WalkStatus): Walk =
        WalkingPlanFactory.create(route(routeId), id, 0.0, 1.0).copy(status = status)

    private fun route(id: String) = Route(
        id = id,
        name = id,
        officialName = id,
        totalDistanceKm = 1.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0))),
        stages = emptyList()
    )
}
