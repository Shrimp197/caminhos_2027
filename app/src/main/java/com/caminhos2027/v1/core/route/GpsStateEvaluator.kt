package com.caminhos2027.v1.core.route

import kotlin.math.abs
import kotlin.math.max
import java.time.Instant

/**
 * Evaluates a sequence of projected GPS observations without depending on Android APIs.
 * GPS quality and route deviation remain separate: weak accuracy alone never means off-route.
 */
object GpsStateEvaluator {
    fun update(
        previous: GpsTrackingState,
        observation: GpsObservation?,
        now: Instant,
        policy: GpsTrackingPolicy = GpsTrackingPolicy()
    ): GpsTrackingState {
        if (observation == null) {
            val last = previous.lastObservation
            val elapsed = if (last == null) Long.MAX_VALUE else max(0, now.epochSecond - last.capturedAt.epochSecond)
            return if (elapsed >= policy.noSignalAfterSeconds) {
                previous.copy(state = GpsState.NO_SIGNAL)
            } else {
                previous.copy(state = GpsState.ACQUIRING)
            }
        }

        if (observation.capturedAt.isAfter(now.plusSeconds(policy.maxFutureSkewSeconds))) {
            return previous
        }

        val lastObservation = previous.lastObservation
        if (lastObservation != null && observation.capturedAt.isBefore(lastObservation.capturedAt)) {
            return previous
        }

        val lastReliable = previous.lastReliableObservation
        val jumpIsPlausible = lastReliable == null || isPlausibleJump(lastReliable, observation, policy)
        if (!jumpIsPlausible) {
            return previous.copy(
                state = previous.state.takeUnless { it == GpsState.NO_SIGNAL } ?: GpsState.ACQUIRING,
                lastObservation = observation
            )
        }

        val distance = observation.routePosition.distanceToRouteMeters
        val probable = distance >= policy.probableDeviationMeters
        val suspicious = distance >= policy.possibleDeviationMeters
        val nextSuspicious = if (suspicious) previous.consecutiveSuspiciousSamples + 1 else 0
        val nextProbable = if (probable) previous.consecutiveProbableSamples + 1 else 0
        val nextState = when {
            nextProbable >= policy.probableDeviationSamples -> GpsState.PROBABLE_DEVIATION
            nextSuspicious >= policy.possibleDeviationSamples -> GpsState.POSSIBLE_DEVIATION
            else -> GpsState.ON_ROUTE
        }

        return if (!suspicious) {
            previous.copy(
                state = nextState,
                lastReliableObservation = observation,
                lastObservation = observation,
                consecutiveSuspiciousSamples = nextSuspicious,
                consecutiveProbableSamples = nextProbable
            )
        } else {
            // A suspicious sample may keep the public state temporarily as ON_ROUTE while
            // hysteresis accumulates. It must not replace the last route position considered reliable.
            previous.copy(
                state = nextState,
                lastObservation = observation,
                consecutiveSuspiciousSamples = nextSuspicious,
                consecutiveProbableSamples = nextProbable
            )
        }
    }

    private fun isPlausibleJump(
        previous: GpsObservation,
        current: GpsObservation,
        policy: GpsTrackingPolicy
    ): Boolean {
        val seconds = current.capturedAt.epochSecond - previous.capturedAt.epochSecond
        if (seconds <= 0) return false
        val distanceKm = abs(current.routePosition.routeKm - previous.routePosition.routeKm)
        val speedKmh = distanceKm / (seconds / 3600.0)
        return speedKmh <= policy.maxPlausibleSpeedKmh
    }
}
