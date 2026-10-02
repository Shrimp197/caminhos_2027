package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RouteGeometry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGeometryValidatorTest {
    @Test
    fun acceptsOrderedFiniteGeometry() {
        assertTrue(
            RouteGeometryValidator.isValid(
                RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.001, -8.001)))
            )
        )
    }

    @Test
    fun rejectsGeometryWithTooFewPoints() {
        assertFalse(RouteGeometryValidator.isValid(RouteGeometry(listOf(GeoPoint(40.0, -8.0)))))
    }

    @Test
    fun rejectsInvalidCoordinates() {
        assertFalse(
            RouteGeometryValidator.isValid(
                RouteGeometry(listOf(GeoPoint(91.0, -8.0), GeoPoint(40.001, -8.001)))
            )
        )
    }

    @Test
    fun rejectsConsecutiveDuplicatePoints() {
        val point = GeoPoint(40.0, -8.0)
        assertFalse(RouteGeometryValidator.isValid(RouteGeometry(listOf(point, point))))
    }
}
