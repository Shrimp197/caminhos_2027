package com.caminhos2027.v1.ui

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
