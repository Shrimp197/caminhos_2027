package com.caminhos2027.v1.core.map

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingMapModelTest {
    @Test fun emptyGeometryIsNotPresentedAsAnOfficialMap() {
        val model = WalkingMapModelBuilder.build(emptyList(), null)
        assertFalse(model.hasOfficialGeometry)
    }

    @Test fun officialGeometryRequiresAtLeastTwoOrderedPoints() {
        val onePoint = WalkingMapModelBuilder.build(listOf(GeoPoint(41.0, -8.0)), null)
        val valid = WalkingMapModelBuilder.build(
            listOf(GeoPoint(41.0, -8.0), GeoPoint(41.01, -8.01)),
            RoutePosition("route", 1.0, 2.0, null, PositionConfidence.HIGH)
        )

        assertFalse(onePoint.hasOfficialGeometry)
        assertTrue(valid.hasOfficialGeometry)
        assertTrue(valid.position != null)
    }
}
