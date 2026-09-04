package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.route.WalkingProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingRouteProgressSurfaceTest {
    @Test
    fun progressPresentationClampsRatioAndShowsRouteDistance() {
        val progress = WalkingProgress(
            currentRouteKm = 42.35,
            walkedKm = 42.35,
            remainingKm = 169.22,
            targetRouteKm = 211.57,
            progressRatio = 1.2,
            stageId = "stage-3",
            stageName = "Etapa 3"
        )

        val presentation = WalkingRouteProgressPresenter.present(progress)

        assertEquals(1.0f, presentation.ratio, 0.0f)
        assertEquals("42.4 km", presentation.currentLabel)
        assertEquals("169.2 km restantes", presentation.remainingLabel)
        assertEquals("Destino 211.6 km", presentation.destinationLabel)
    }

    @Test
    fun negativeRatioIsClampedToZero() {
        val progress = WalkingProgress(
            currentRouteKm = 0.0,
            walkedKm = 0.0,
            remainingKm = 211.87,
            targetRouteKm = 211.87,
            progressRatio = -0.4,
            stageId = null
        )

        assertEquals(0.0f, WalkingRouteProgressPresenter.present(progress).ratio, 0.0f)
    }

    @Test
    fun ordinaryProgressRatioRemainsVisibleAsFraction() {
        val progress = WalkingProgress(
            currentRouteKm = 50.0,
            walkedKm = 50.0,
            remainingKm = 50.0,
            targetRouteKm = 100.0,
            progressRatio = 0.5,
            stageId = "stage-1"
        )

        assertEquals(0.5f, WalkingRouteProgressPresenter.present(progress).ratio, 0.0f)
        assertTrue(WalkingRouteProgressPresenter.present(progress).remainingLabel.contains("50.0 km"))
    }
}
