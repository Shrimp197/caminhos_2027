package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Regression boundary: an active session from another route must never be resumed here. */
class WalkingSessionRuntimeRouteBoundaryTest {
    private val publishedRoute = route("published-route")
    private val foreignRoute = route("foreign-route")

    @Test
    fun resumeRejectsActiveSessionFromForeignRouteWithoutAttachingIt() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)

        val foreignWalk = WalkingPlanFactory.create(
            foreignRoute,
            "foreign-active",
            startRouteKm = 0.2,
            destinationRouteKm = 1.8
        )
        service.prepare(foreignWalk)
        service.start(
            foreignWalk.id,
            RoutePosition(
                routeId = foreignRoute.id,
                routeKm = 0.2,
                distanceToRouteMeters = 2.0,
                stageId = "foreign-stage-1",
                confidence = PositionConfidence.HIGH
            ),
            Instant.parse("2026-09-04T08:00:00Z")
        )

        val runtime = WalkingSessionRuntime(publishedRoute, service, emptyList())
        val activeBefore = walks.getById(foreignWalk.id)

        try {
            runtime.resume(Instant.parse("2026-09-04T08:01:00Z"))
            throw AssertionError("Expected cross-route resume to be rejected")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("route"))
        }

        assertEquals(activeBefore, walks.getById(foreignWalk.id))
        assertEquals(WalkStatus.ACTIVE, walks.getById(foreignWalk.id)!!.status)
        try {
            runtime.accept(
                RawGpsPosition(
                    latitude = 40.001,
                    longitude = -8.0,
                    accuracyMeters = 5.0,
                    capturedAt = Instant.parse("2026-09-04T08:01:10Z")
                )
            )
            throw AssertionError("Rejected resume must not attach a coordinator")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("not been started"))
        }
        assertEquals(activeBefore, walks.getById(foreignWalk.id))
    }

    @Test
    fun rejectedResumeDoesNotDetachAnAlreadyRunningPublishedCoordinator() {
        val walks = SelectableActiveWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val publishedWalk = WalkingPlanFactory.create(publishedRoute, "published-active", 0.2, 1.8)
        val foreignWalk = WalkingPlanFactory.create(foreignRoute, "foreign-active", 0.2, 1.8)

        service.prepare(publishedWalk)
        val runtime = WalkingSessionRuntime(publishedRoute, service, emptyList())
        runtime.start(
            publishedWalk.id,
            RoutePosition(publishedRoute.id, 0.2, 2.0, "published-route-stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-04T09:00:00Z")
        )

        service.prepare(foreignWalk)
        service.start(
            foreignWalk.id,
            RoutePosition(foreignRoute.id, 0.2, 2.0, "foreign-route-stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-04T09:01:00Z")
        )
        walks.activeId = foreignWalk.id

        try {
            runtime.resume(Instant.parse("2026-09-04T09:02:00Z"))
            throw AssertionError("Expected cross-route resume to be rejected")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("route"))
        }

        val continued = runtime.accept(
            RawGpsPosition(
                latitude = 40.0045,
                longitude = -8.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-04T09:02:10Z")
            )
        )
        assertEquals(publishedWalk.id, continued.walk.id)
        assertEquals(publishedRoute.id, continued.routePosition?.routeId)
        assertEquals(WalkStatus.ACTIVE, continued.walk.status)
    }

    @Test
    fun resumeWithoutPersistedActiveWalkDetachesStaleCoordinator() {
        val walks = SelectableActiveWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val walk = WalkingPlanFactory.create(publishedRoute, "published-active", 0.2, 1.8)
        val runtime = WalkingSessionRuntime(publishedRoute, service, emptyList())

        service.prepare(walk)
        runtime.start(
            walk.id,
            RoutePosition(publishedRoute.id, 0.2, 2.0, "published-route-stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-04T10:00:00Z")
        )
        walks.activeId = null

        assertEquals(null, runtime.resume(Instant.parse("2026-09-04T10:01:00Z")))

        try {
            runtime.accept(
                RawGpsPosition(
                    latitude = 40.0045,
                    longitude = -8.0,
                    accuracyMeters = 5.0,
                    capturedAt = Instant.parse("2026-09-04T10:01:10Z")
                )
            )
            throw AssertionError("A stale coordinator must not survive a missing persisted session")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("not been started"))
        }
    }

    @Test
    fun activeWalkInspectionDoesNotCreateOrReplaceCoordinator() {
        val walks = SelectableActiveWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val walk = WalkingPlanFactory.create(publishedRoute, "published-active", 0.2, 1.8)
        val runtime = WalkingSessionRuntime(publishedRoute, service, emptyList())

        service.prepare(walk)
        runtime.start(
            walk.id,
            RoutePosition(publishedRoute.id, 0.2, 2.0, "published-route-stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-04T11:00:00Z")
        )

        assertEquals(walk.id, runtime.activeWalk()?.id)
        walks.activeId = null
        assertEquals(null, runtime.activeWalk())

        val continued = runtime.accept(
            RawGpsPosition(
                latitude = 40.0045,
                longitude = -8.0,
                accuracyMeters = 5.0,
                capturedAt = Instant.parse("2026-09-04T11:01:00Z")
            )
        )
        assertEquals(walk.id, continued.walk.id)
    }

    private class SelectableActiveWalkRepository : WalkRepository {
        private val walks = linkedMapOf<String, Walk>()
        var activeId: String? = null

        override fun save(walk: Walk) {
            walks[walk.id] = walk
            if (walk.status == WalkStatus.ACTIVE && activeId == null) activeId = walk.id
        }

        override fun getById(id: String): Walk? = walks[id]
        override fun getActive(): Walk? = activeId?.let(walks::get)
        override fun list(): List<Walk> = walks.values.toList()
    }

    private fun route(id: String) = Route(
        id,
        id,
        "Synthetic $id",
        2.0,
        "TEST",
        "2026-09-01",
        RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.009, -8.0),
                GeoPoint(40.018, -8.0)
            )
        ),
        listOf(
            Stage("$id-stage-1", id, 1, "Stage 1", 0.0, 1.0, 1.0, "A", "B", "TEST"),
            Stage("$id-stage-2", id, 2, "Stage 2", 1.0, 2.0, 1.0, "B", "C", "TEST")
        )
    )
}
