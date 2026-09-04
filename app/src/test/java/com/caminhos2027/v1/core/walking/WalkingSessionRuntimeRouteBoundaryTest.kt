package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
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
                    observedAt = Instant.parse("2026-09-04T08:01:10Z")
                )
            )
            throw AssertionError("Rejected resume must not attach a coordinator")
        } catch (error: IllegalArgumentException) {
            assertTrue(error.message.orEmpty().contains("not been started"))
        }
        assertEquals(activeBefore, walks.getById(foreignWalk.id))
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
