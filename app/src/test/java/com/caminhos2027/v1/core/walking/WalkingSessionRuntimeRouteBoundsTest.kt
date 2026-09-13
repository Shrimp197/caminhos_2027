package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import java.time.Instant
import org.junit.Test

class WalkingSessionRuntimeRouteBoundsTest {
    private val route = Route(
        id = "bounded-route",
        name = "TEST/FICTITIOUS bounded route",
        officialName = "TEST/FICTITIOUS bounded route",
        totalDistanceKm = 2.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-04",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.018, -8.0))),
        stages = listOf(
            Stage("stage-1", "bounded-route", 1, "TEST/FICTITIOUS", 0.0, 2.0, 2.0, "A", "B", "TEST/FICTITIOUS")
        )
    )

    private fun runtime(walkId: String): WalkingSessionRuntime {
        val repository = InMemoryWalkRepository()
        val service = WalkingSessionService(repository, InMemoryWalkingStateRepository())
        return WalkingSessionRuntime(route, service, emptyList()).also {
            it.prepare(WalkingPlanFactory.create(route, walkId, 0.0, 2.0))
        }
    }

    private fun position(routeKm: Double) = RoutePosition(
        routeId = route.id,
        routeKm = routeKm,
        distanceToRouteMeters = 0.0,
        stageId = "stage-1",
        confidence = PositionConfidence.HIGH
    )

    @Test(expected = IllegalArgumentException::class)
    fun startRejectsPositionBeyondPublishedRouteLength() {
        runtime("walk-start-bounds").start(
            "walk-start-bounds",
            position(2.000001),
            Instant.parse("2026-09-04T08:00:00Z")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun stopRejectsPositionBeyondPublishedRouteLength() {
        val runtime = runtime("walk-stop-bounds")
        runtime.start("walk-stop-bounds", position(0.5), Instant.parse("2026-09-04T08:00:00Z"))
        runtime.stop(position(2.000001), Instant.parse("2026-09-04T09:00:00Z"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun startRejectsPositionFromAnotherRouteBeforeLifecycleMutation() {
        runtime("walk-route-bounds").start(
            "walk-route-bounds",
            position(0.5).copy(routeId = "other-route"),
            Instant.parse("2026-09-04T08:00:00Z")
        )
    }
}
