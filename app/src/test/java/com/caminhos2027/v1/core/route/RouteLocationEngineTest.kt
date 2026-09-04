package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
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
        assertEquals(PositionConfidence.HIGH, position.confidence)
    }

    @Test
    fun weakAccuracyProducesMediumConfidenceWhenProjectionRemainsNearRoute() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0003, -7.995, accuracyMeters = 50.0))

        assertTrue(position.distanceToRouteMeters < 80.0)
        assertEquals(PositionConfidence.MEDIUM, position.confidence)
    }

    @Test
    fun farProjectionProducesLowConfidence() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.001, -7.995))

        assertTrue(position.distanceToRouteMeters > 80.0)
        assertEquals(PositionConfidence.LOW, position.confidence)
    }

    @Test
    fun missingAccuracyLeavesConfidenceUnknown() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.995, accuracyMeters = null))

        assertEquals(PositionConfidence.UNKNOWN, position.confidence)
    }

    @Test
    fun nonFiniteAccuracyLeavesConfidenceUnknown() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.995, accuracyMeters = Double.NaN))

        assertEquals(PositionConfidence.UNKNOWN, position.confidence)
    }

    @Test
    fun negativeAccuracyLeavesConfidenceUnknown() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.995, accuracyMeters = -1.0))

        assertEquals(PositionConfidence.UNKNOWN, position.confidence)
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

    private fun gps(latitude: Double, longitude: Double, accuracyMeters: Double? = 5.0) = RawGpsPosition(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
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
}
