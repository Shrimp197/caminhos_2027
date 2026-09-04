package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.model.GeoPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WalkingRouteOverviewSurfaceTest {
    @Test
    fun currentRatioClampsToRouteBounds() {
        assertEquals(0f, WalkingRouteOverviewPresenter.currentRatio(-1.0, 200.0))
        assertEquals(0.5f, WalkingRouteOverviewPresenter.currentRatio(100.0, 200.0))
        assertEquals(1f, WalkingRouteOverviewPresenter.currentRatio(250.0, 200.0))
    }

    @Test
    fun currentRatioRejectsInvalidTotalsAndPositions() {
        assertEquals(0f, WalkingRouteOverviewPresenter.currentRatio(null, 200.0))
        assertEquals(0f, WalkingRouteOverviewPresenter.currentRatio(Double.NaN, 200.0))
        assertEquals(0f, WalkingRouteOverviewPresenter.currentRatio(50.0, 0.0))
        assertEquals(0f, WalkingRouteOverviewPresenter.currentRatio(50.0, Double.NaN))
    }

    @Test
    fun visiblePathIndexClampsAndHandlesSmallGeometry() {
        assertEquals(0, WalkingRouteOverviewPresenter.visiblePathPointIndex(0, 0.5f))
        assertEquals(0, WalkingRouteOverviewPresenter.visiblePathPointIndex(1, 0.5f))
        assertEquals(0, WalkingRouteOverviewPresenter.visiblePathPointIndex(5, -1f))
        assertEquals(2, WalkingRouteOverviewPresenter.visiblePathPointIndex(5, 0.5f))
        assertEquals(4, WalkingRouteOverviewPresenter.visiblePathPointIndex(5, 2f))
    }

    @Test
    fun distanceAwareIndexFollowsRouteLengthInsteadOfPointCount() {
        val geometry = listOf(
            GeoPoint(41.0, -8.0),
            GeoPoint(41.001, -8.0),
            GeoPoint(41.001, -8.02)
        )

        assertEquals(2, WalkingRouteOverviewPresenter.visiblePathPointIndex(geometry, 0.5f))
        assertEquals(2, WalkingRouteOverviewPresenter.visiblePathPointIndex(geometry, 0.9f))
    }

    @Test
    fun geometryProfileCachesCumulativeMetersForSafePoints() {
        val geometry = listOf(
            GeoPoint(41.0, -8.0),
            GeoPoint(Double.NaN, -8.1),
            GeoPoint(41.001, -8.0)
        )

        val profile = WalkingRouteOverviewPresenter.buildGeometryProfile(geometry)

        assertEquals(2, profile.points.size)
        assertEquals(2, profile.cumulativeMeters.size)
        assertEquals(0.0, profile.cumulativeMeters.first())
        assertEquals(profile.totalMeters, profile.cumulativeMeters.last())
        assertEquals(2, WalkingRouteOverviewPresenter.visiblePathPointIndex(profile, 1f))
    }

    @Test
    fun distanceAwareIndexFallsBackForCollapsedGeometry() {
        val geometry = listOf(
            GeoPoint(41.0, -8.0),
            GeoPoint(41.0, -8.0),
            GeoPoint(41.0, -8.0)
        )

        val profile = WalkingRouteOverviewPresenter.buildGeometryProfile(geometry)
        assertEquals(0.0, profile.totalMeters)
        assertEquals(1, WalkingRouteOverviewPresenter.visiblePathPointIndex(profile, 0.5f))
        assertEquals(2, WalkingRouteOverviewPresenter.visiblePathPointIndex(profile, 1.0f))
    }

    @Test
    fun routeBearingAndDirectionDescribeTheCurrentRouteSegment() {
        val geometry = listOf(
            GeoPoint(40.0, -8.0),
            GeoPoint(40.01, -8.0),
            GeoPoint(40.01, -7.99)
        )

        assertEquals("Norte", WalkingRouteOverviewPresenter.routeDirectionLabel(
            WalkingRouteOverviewPresenter.routeBearingDegrees(geometry, 0f)
        ))
        assertEquals("Este", WalkingRouteOverviewPresenter.routeDirectionLabel(
            WalkingRouteOverviewPresenter.routeBearingDegrees(geometry, 0.9f)
        ))
    }

    @Test
    fun cachedProfileProducesSameBearingAsGeometryHelper() {
        val geometry = listOf(
            GeoPoint(40.0, -8.0),
            GeoPoint(40.01, -8.0),
            GeoPoint(40.01, -7.99)
        )
        val profile = WalkingRouteOverviewPresenter.buildGeometryProfile(geometry)

        assertEquals(
            WalkingRouteOverviewPresenter.routeBearingDegrees(geometry, 0.25f),
            WalkingRouteOverviewPresenter.routeBearingDegrees(profile, 0.25f)
        )
    }

    @Test
    fun routeDirectionRejectsDegenerateOrInvalidGeometry() {
        assertNull(WalkingRouteOverviewPresenter.routeBearingDegrees(emptyList(), 0.5f))
        assertNull(WalkingRouteOverviewPresenter.routeBearingDegrees(listOf(GeoPoint(40.0, -8.0)), 0.5f))
        assertNull(WalkingRouteOverviewPresenter.routeBearingDegrees(listOf(
            GeoPoint(40.0, -8.0),
            GeoPoint(40.0, -8.0)
        ), 0.5f))
        assertNull(WalkingRouteOverviewPresenter.routeDirectionLabel(Double.NaN))
    }

    @Test
    fun sanitizeGeometryDropsNonFinitePoints() {
        val geometry = listOf(
            GeoPoint(40.0, -8.0),
            GeoPoint(Double.NaN, -8.1),
            GeoPoint(39.9, Double.POSITIVE_INFINITY),
            GeoPoint(39.8, -8.2)
        )

        assertEquals(
            listOf(GeoPoint(40.0, -8.0), GeoPoint(39.8, -8.2)),
            WalkingRouteOverviewPresenter.sanitizeGeometry(geometry)
        )
    }
}
