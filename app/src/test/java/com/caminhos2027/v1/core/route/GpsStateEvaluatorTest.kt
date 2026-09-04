package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GpsStateEvaluatorTest {
    private val t0 = Instant.parse("2026-09-02T00:00:00Z")

    @Test
    fun stableOnRouteSequenceStaysOnRoute() {
        var state = GpsTrackingState(GpsState.ACQUIRING)
        state = GpsStateEvaluator.update(state, observation(0.5, 8.0, 5.0, t0), t0)
        state = GpsStateEvaluator.update(state, observation(0.51, 10.0, 5.0, t0.plusSeconds(10)), t0.plusSeconds(10))

        assertEquals(GpsState.ON_ROUTE, state.state)
        assertEquals(0.51, state.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test
    fun oneNoisyPointDoesNotBecomeProbableDeviation() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        state = GpsStateEvaluator.update(state, observation(0.5, 90.0, 5.0, t0), t0)

        assertTrue(state.state != GpsState.PROBABLE_DEVIATION)
        assertEquals(1, state.consecutiveProbableSamples)
    }

    @Test
    fun repeatedOffRoutePointsEscalate() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        state = GpsStateEvaluator.update(state, observation(0.5, 40.0, 5.0, t0), t0)
        assertEquals(GpsState.ON_ROUTE, state.state)
        state = GpsStateEvaluator.update(state, observation(0.51, 45.0, 5.0, t0.plusSeconds(10)), t0.plusSeconds(10))
        assertEquals(GpsState.POSSIBLE_DEVIATION, state.state)
        state = GpsStateEvaluator.update(state, observation(0.52, 90.0, 5.0, t0.plusSeconds(20)), t0.plusSeconds(20))
        state = GpsStateEvaluator.update(state, observation(0.53, 90.0, 5.0, t0.plusSeconds(30)), t0.plusSeconds(30))
        state = GpsStateEvaluator.update(state, observation(0.54, 90.0, 5.0, t0.plusSeconds(40)), t0.plusSeconds(40))
        assertEquals(GpsState.PROBABLE_DEVIATION, state.state)
    }

    @Test
    fun returningToRouteRecoversState() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        repeat(3) { index ->
            val time = t0.plusSeconds(index * 10L)
            state = GpsStateEvaluator.update(state, observation(0.5 + index * 0.01, 90.0, 5.0, time), time)
        }
        state = GpsStateEvaluator.update(state, observation(0.54, 90.0, 5.0, t0.plusSeconds(30)), t0.plusSeconds(30))
        state = GpsStateEvaluator.update(state, observation(0.55, 90.0, 5.0, t0.plusSeconds(40)), t0.plusSeconds(40))
        state = GpsStateEvaluator.update(state, observation(0.56, 90.0, 5.0, t0.plusSeconds(50)), t0.plusSeconds(50))
        assertEquals(GpsState.PROBABLE_DEVIATION, state.state)
        state = GpsStateEvaluator.update(state, observation(0.57, 6.0, 5.0, t0.plusSeconds(60)), t0.plusSeconds(60))
        assertEquals(GpsState.ON_ROUTE, state.state)
        assertEquals(0, state.consecutiveSuspiciousSamples)
    }

    @Test
    fun weakAccuracyDoesNotByItselfMeanDeviation() {
        val state = GpsStateEvaluator.update(
            GpsTrackingState(GpsState.ACQUIRING),
            observation(0.5, 5.0, 100.0, t0),
            t0
        )

        assertEquals(GpsState.ON_ROUTE, state.state)
    }

    @Test
    fun noSignalPreservesLastReliablePosition() {
        var state = GpsTrackingState(GpsState.ACQUIRING)
        state = GpsStateEvaluator.update(state, observation(0.5, 5.0, 5.0, t0), t0)
        state = GpsStateEvaluator.update(state, null, t0.plusSeconds(31))

        assertEquals(GpsState.NO_SIGNAL, state.state)
        assertSame(state.lastReliableObservation, state.lastObservation)
        assertEquals(0.5, state.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test
    fun implausibleJumpDoesNotMoveReliablePosition() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        state = GpsStateEvaluator.update(state, observation(1.0, 5.0, 5.0, t0), t0)
        state = GpsStateEvaluator.update(state, observation(3.0, 5.0, 5.0, t0.plusSeconds(10)), t0.plusSeconds(10))

        assertEquals(1.0, state.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test
    fun shortSubsecondObservationUsesElapsedMilliseconds() {
        var state = GpsTrackingState(GpsState.ACQUIRING)
        state = GpsStateEvaluator.update(state, observation(0.5, 5.0, 5.0, t0), t0)
        state = GpsStateEvaluator.update(
            state,
            observation(0.5005, 5.0, 5.0, t0.plusMillis(500)),
            t0.plusMillis(500)
        )

        assertEquals(0.5005, state.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.00001)
    }

    @Test
    fun outOfOrderObservationIsIgnoredForStateAndSignalClock() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        state = GpsStateEvaluator.update(state, observation(1.0, 5.0, 5.0, t0.plusSeconds(20)), t0.plusSeconds(20))
        state = GpsStateEvaluator.update(state, observation(1.1, 5.0, 5.0, t0.plusSeconds(10)), t0.plusSeconds(21))

        assertEquals(1.0, state.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(1.0, state.lastObservation?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(t0.plusSeconds(20), state.lastObservation?.capturedAt)
    }

    @Test
    fun malformedRouteMetricsAreIgnored() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        state = GpsStateEvaluator.update(state, observation(0.5, 5.0, 5.0, t0), t0)
        val result = GpsStateEvaluator.update(
            state,
            observation(Double.NaN, 5.0, 5.0, t0.plusSeconds(10)),
            t0.plusSeconds(10)
        )

        assertSame(state, result)
        assertEquals(0.5, result.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.001)
    }

    @Test
    fun malformedDistanceOrAccuracyIsIgnored() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        state = GpsStateEvaluator.update(state, observation(0.5, 5.0, 5.0, t0), t0)
        val badDistance = GpsStateEvaluator.update(
            state,
            observation(0.6, Double.POSITIVE_INFINITY, 5.0, t0.plusSeconds(10)),
            t0.plusSeconds(10)
        )
        assertSame(state, badDistance)

        val badAccuracy = GpsStateEvaluator.update(
            state,
            observation(0.6, 5.0, -1.0, t0.plusSeconds(10)),
            t0.plusSeconds(10)
        )
        assertSame(state, badAccuracy)
    }

    @Test
    fun materiallyFutureObservationIsIgnored() {
        var state = GpsTrackingState(GpsState.ON_ROUTE)
        val now = t0.plusSeconds(10)
        state = GpsStateEvaluator.update(state, observation(0.5, 5.0, 5.0, t0), t0)
        val result = GpsStateEvaluator.update(
            state,
            observation(4.0, 5.0, 5.0, now.plusSeconds(16)),
            now
        )

        assertSame(state, result)
        assertEquals(0.5, result.lastReliableObservation?.routePosition?.routeKm ?: -1.0, 0.001)
        assertEquals(t0, result.lastObservation?.capturedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidNegativeSignalTimeoutPolicyIsRejected() {
        GpsTrackingPolicy(noSignalAfterSeconds = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidDeviationOrderingPolicyIsRejected() {
        GpsTrackingPolicy(possibleDeviationMeters = 100.0, probableDeviationMeters = 50.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonFiniteSpeedPolicyIsRejected() {
        GpsTrackingPolicy(maxPlausibleSpeedKmh = Double.NaN)
    }

    private fun observation(
        routeKm: Double,
        distanceToRouteMeters: Double,
        accuracyMeters: Double,
        capturedAt: Instant
    ) = GpsObservation(
        routePosition = RoutePosition(
            routeId = "test-route",
            routeKm = routeKm,
            distanceToRouteMeters = distanceToRouteMeters,
            stageId = "stage-1",
            confidence = PositionConfidence.UNKNOWN
        ),
        accuracyMeters = accuracyMeters,
        capturedAt = capturedAt
    )
}
