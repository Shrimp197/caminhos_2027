package com.caminhos2027.v1.core.route

import kotlin.test.Test
import kotlin.test.assertEquals

class WalkingMovementCueEvaluatorTest {
    @Test
    fun classifiesForwardMovementAlongRoute() {
        assertEquals(
            WalkingMovementCue.FORWARD,
            WalkingMovementCueEvaluator.evaluate(12.0, 12.25)
        )
    }

    @Test
    fun classifiesBackwardMovementAlongRoute() {
        assertEquals(
            WalkingMovementCue.BACKWARD,
            WalkingMovementCueEvaluator.evaluate(12.25, 12.0)
        )
    }

    @Test
    fun treatsSmallProgressAsStationary() {
        assertEquals(
            WalkingMovementCue.STATIONARY,
            WalkingMovementCueEvaluator.evaluate(12.0, 12.01)
        )
    }

    @Test
    fun rejectsMissingOrInvalidMeasurements() {
        assertEquals(WalkingMovementCue.UNKNOWN, WalkingMovementCueEvaluator.evaluate(null, 1.0))
        assertEquals(WalkingMovementCue.UNKNOWN, WalkingMovementCueEvaluator.evaluate(1.0, Double.NaN))
        assertEquals(WalkingMovementCue.UNKNOWN, WalkingMovementCueEvaluator.evaluate(1.0, 2.0, Double.NaN))
        assertEquals(WalkingMovementCue.UNKNOWN, WalkingMovementCueEvaluator.evaluate(1.0, 2.0, -0.1))
    }
}
