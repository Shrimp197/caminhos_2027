package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RouteLocationEngineTest {
    @Test
    fun projectsPositionOntoNearestSegment() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.995))

        assertTrue(position.distanceToRouteMeters < 20.0)
        assertEquals("stage-1", position.stageId)
        assertTrue(position.routeKm > 0.4)
        assertTrue(position.routeKm < 0.6)
    }

    @Test
    fun stageBoundaryFromProjectionBelongsToNextStage() {
        val route = fixtureWithStageBoundary()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.994135))

        assertEquals("stage-2", position.stageId)
        assertEquals(0.5, position.routeKm, 0.03)
    }

    @Test
    fun positionBeforeSegmentClampsToSegmentStart() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -8.01))

        assertEquals(0.0, position.routeKm, 0.05)
    }

    @Test
    fun positionAfterSegmentClampsToSegmentEnd() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.98))

        assertEquals(1.0, position.routeKm, 0.05)
    }

    private fun gps(latitude: Double, longitude: Double) = RawGpsPosition(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = 5.0,
        capturedAt = Instant.parse("2026-09-02T00:00:00Z")
    )

    private fun fixture() = Route(
        id = "test-route",
        name = "TEST/FICTITIOUS route",
        officialName = "TEST/FICTITIOUS route",
        totalDistanceKm = 1.0,
        source = "TEST/FICTITIOUS",
        updatedAt = "2026-09-02",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.0, -7.98827)
            )
        ),
        stages = listOf(
            Stage(
                id = "stage-1",
                routeId = "test-route",
                number = 1,
                name = "TEST/FICTITIOUS stage",
                startRouteKm = 0.0,
                endRouteKm = 1.0,
                distanceKm = 1.0,
                startName = "TEST/FICTITIOUS start",
                endName = "TEST/FICTITIOUS end",
                source = "TEST/FICTITIOUS"
            )
        )
    )

    private fun fixtureWithStageBoundary() = fixture().copy(
        stages = listOf(
            fixture().stages[0].copy(endRouteKm = 0.5, distanceKm = 0.5),
            fixture().stages[0].copy(
                id = "stage-2",
                number = 2,
                startRouteKm = 0.5,
                endRouteKm = 1.0,
                distanceKm = 0.5
            )
        )
    )
}
