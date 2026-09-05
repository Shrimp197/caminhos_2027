package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RouteGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGeometryMetricsTest {
    @Test
    fun emptyGeometryHasZeroLength() {
        assertEquals(0.0, RouteGeometryMetrics.lengthKm(RouteGeometry(emptyList())), 0.0)
    }

    @Test
    fun singlePointHasZeroLength() {
        assertEquals(0.0, RouteGeometryMetrics.lengthKm(RouteGeometry(listOf(GeoPoint(40.0, -8.0)))), 0.0)
    }

    @Test
    fun repeatedPointsDoNotAddDistance() {
        val point = GeoPoint(40.0, -8.0)
        assertEquals(0.0, RouteGeometryMetrics.lengthKm(RouteGeometry(listOf(point, point))), 0.0)
    }

    @Test
    fun knownShortSegmentHasPlausibleLength() {
        val geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.0, -7.99)
            )
        )
        val length = RouteGeometryMetrics.lengthKm(geometry)
        assertTrue("Expected roughly 0.85 km, got $length", length in 0.8..0.9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonFiniteCoordinateIsRejected() {
        RouteGeometryMetrics.lengthKm(
            RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(Double.NaN, -7.99)))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun outOfBoundsCoordinateIsRejected() {
        RouteGeometryMetrics.lengthKm(
            RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(91.0, -7.99)))
        )
    }

    @Test
    fun tinyHaversineRoundingRemainsFinite() {
        val point = GeoPoint(40.0, -8.0)
        val nearby = GeoPoint(40.0, -7.999999999)
        assertTrue(RouteGeometryMetrics.lengthKm(RouteGeometry(listOf(point, nearby))).isFinite())
    }
}
