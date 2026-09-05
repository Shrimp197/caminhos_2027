package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.RoutePosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.Instant

class GpsStateEvaluatorRobustnessTest {
    private val policy = GpsTrackingPolicy(
        noSignalAfterSeconds = 30,
        possibleDeviationMeters = 35.0,
        probableDeviationMeters = 80.0,
        possibleDeviationSamples = 2,
        probableDeviationSamples = 3,
        maxPlausibleSpeedKmh = 8.0
    )

    @Test
    fun implausibleJumpDoesNotBecomeReliableProgress() {
        val previous = state(10.0, 40.0)
        val jump = observation(11.0, 40.0, 10)

        val result = GpsStateEvaluator.update(previous, jump, Instant.parse("2026-09-02T00:00:10Z"), policy)

        assertEquals(10.0, result.lastReliableObservation!!.routePosition.routeKm, 0.001)
        assertSame(jump, result.lastObservation)
    }

    @Test
    fun repeatedSuspiciousObservationsBecomePossibleDeviation() {
        val initial = state(10.0, 40.0)
        val first = observation(10.01, 40.0, 5)
        val second = observation(10.02, 40.0, 10)

        val afterFirst = GpsStateEvaluator.update(initial, first, Instant.parse("2026-09-02T00:00:05Z"), policy)
        val afterSecond = GpsStateEvaluator.update(afterFirst, second, Instant.parse("2026-09-02T00:00:10Z"), policy)

        assertEquals(GpsState.ON_ROUTE, afterFirst.state)
        assertEquals(10.0, afterFirst.lastReliableObservation!!.routePosition.routeKm, 0.001)
        assertEquals(GpsState.POSSIBLE_DEVIATION, afterSecond.state)
        assertEquals(10.0, afterSecond.lastReliableObservation!!.routePosition.routeKm, 0.001)
        assertSame(second, afterSecond.lastObservation)
    }

    @Test
    fun repeatedProbableObservationsBecomeProbableDeviationWithoutReplacingReliablePosition() {
        val initial = state(10.0, 20.0)
        val first = observation(10.01, 90.0, 5)
        val second = observation(10.02, 90.0, 10)
        val third = observation(10.03, 90.0, 15)

        val afterFirst = GpsStateEvaluator.update(initial, first, Instant.parse("2026-09-02T00:00:05Z"), policy)
        val afterSecond = GpsStateEvaluator.update(afterFirst, second, Instant.parse("2026-09-02T00:00:10Z"), policy)
        val afterThird = GpsStateEvaluator.update(afterSecond, third, Instant.parse("2026-09-02T00:00:15Z"), policy)

        assertEquals(GpsState.ON_ROUTE, afterFirst.state)
        assertEquals(GpsState.POSSIBLE_DEVIATION, afterSecond.state)
        assertEquals(GpsState.PROBABLE_DEVIATION, afterThird.state)
        assertEquals(10.0, afterThird.lastReliableObservation!!.routePosition.routeKm, 0.001)
    }

    @Test
    fun missingSignalEventuallyBecomesNoSignal() {
        val previous = state(10.0, 40.0)

        val result = GpsStateEvaluator.update(
            previous,
            observation = null,
            now = Instant.parse("2026-09-02T00:00:31Z"),
            policy = policy
        )

        assertEquals(GpsState.NO_SIGNAL, result.state)
    }

    private fun state(routeKm: Double, distanceToRouteMeters: Double) = GpsTrackingState(
        state = GpsState.ON_ROUTE,
        lastReliableObservation = observation(routeKm, distanceToRouteMeters),
        lastObservation = observation(routeKm, distanceToRouteMeters)
    )

    private fun observation(routeKm: Double, distanceToRouteMeters: Double, seconds: Long = 0) = GpsObservation(
        routePosition = RoutePosition("test-route", routeKm, distanceToRouteMeters, "stage-1"),
        accuracyMeters = 5.0,
        capturedAt = Instant.parse("2026-09-02T00:00:00Z").plusSeconds(seconds)
    )
}
