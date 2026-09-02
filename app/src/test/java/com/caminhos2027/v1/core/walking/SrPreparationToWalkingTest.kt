package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant

/** SR vertical slice: preparation -> persistence -> start -> resume -> stop. */
class SrPreparationToWalkingTest {
    private val route = Route(
        "sr-route", "SR", "SR synthetic route", 2.0, "SR", "2026-09-01",
        RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0), GeoPoint(40.018, -8.0))),
        listOf(
            Stage("stage-1", "sr-route", 1, "Stage 1", 0.0, 1.0, 1.0, "A", "B", "SR"),
            Stage("stage-2", "sr-route", 2, "Stage 2", 1.0, 2.0, 1.0, "B", "C", "SR")
        )
    )

    @Test
    fun preparedPlanCanBeStartedResumedAndCompleted() {
        val walks = InMemoryWalkRepository()
        val states = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walks, states)
        val preparationService = WalkingPreparationService(route, walks)
        val preparation = preparationService.save("sr-vertical", 0.4, 1.8)

        assertEquals(WalkStatus.PLANNED, preparation.walk.status)
        assertEquals(2, preparation.stages.size)

        val runtime = WalkingSessionRuntime(route, service, emptyList())
        val started = runtime.start(
            preparation.walk.id,
            RoutePosition("sr-route", 0.4, 3.0, "stage-1", PositionConfidence.HIGH),
            Instant.parse("2026-09-01T08:00:00Z")
        )
        assertEquals(WalkStatus.ACTIVE, started.walk.status)
        assertEquals(GpsState.ACQUIRING, started.gpsState)

        val resumed = WalkingSessionRuntime(route, service, emptyList()).resume()
        assertNotNull(resumed)
        assertEquals(0.4, resumed!!.routePosition!!.routeKm, 0.001)

        val completed = WalkingSessionRuntime(route, service, emptyList()).let { activeRuntime ->
            activeRuntime.resume()
            activeRuntime.stop(
                RoutePosition("sr-route", 1.4, 3.0, "stage-2", PositionConfidence.HIGH),
                Instant.parse("2026-09-01T12:00:00Z")
            )
        }
        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertEquals(1.4, completed.actualEndKm!!, 0.001)
    }
}
