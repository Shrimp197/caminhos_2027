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

class RouteLocationEngineRobustnessTest {
    @Test
    fun projectionAcrossTwoSegmentsKeepsRouteKmMonotonic() {
        val route = fixture()
        val first = RouteLocationEngine.locate(route, gps(40.0, -7.997))
        val second = RouteLocationEngine.locate(route, gps(40.002, -7.99))
        val third = RouteLocationEngine.locate(route, gps(40.01, -7.99))

        assertTrue(first.routeKm < second.routeKm)
        assertTrue(second.routeKm < third.routeKm)
    }

    @Test
    fun projectionUsesGeometricDistanceInsteadOfDeclaredRouteDistance() {
        val route = fixture()
        val position = RouteLocationEngine.locate(route, gps(40.0, -7.99))

        assertEquals(0.852, position.routeKm, 0.03)
        assertEquals("stage-1", position.stageId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonFiniteGpsLatitude() {
        RouteLocationEngine.locate(fixture(), gps(Double.NaN, -8.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsGpsLatitudeOutsideEarthBounds() {
        RouteLocationEngine.locate(fixture(), gps(91.0, -8.0))
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
                GeoPoint(40.0, -7.99),
                GeoPoint(40.01, -7.99)
            )
        ),
        stages = listOf(
            Stage(
                id = "stage-1",
                routeId = "test-route",
                number = 1,
                name = "TEST/FICTITIOUS stage",
                startRouteKm = 0.0,
                endRouteKm = 2.0,
                distanceKm = 2.0,
                startName = "TEST/FICTITIOUS start",
                endName = "TEST/FICTITIOUS end",
                source = "TEST/FICTITIOUS"
            )
        )
    )
}
