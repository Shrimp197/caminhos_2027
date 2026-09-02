package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.RoutePosition
import java.time.Instant

/** Device/location quality and route relationship are deliberately separate concepts. */
enum class GpsState {
    NO_SIGNAL,
    ACQUIRING,
    ON_ROUTE,
    POSSIBLE_DEVIATION,
    PROBABLE_DEVIATION
}

data class GpsObservation(
    val routePosition: RoutePosition,
    val accuracyMeters: Double?,
    val capturedAt: Instant
)

data class GpsTrackingPolicy(
    /** Maximum age before a missing update is treated as no signal. Initial test value; tune in HF. */
    val noSignalAfterSeconds: Long = 30,
    /** Distance from route at which an observation becomes suspicious. Initial test value; tune in HF. */
    val possibleDeviationMeters: Double = 35.0,
    /** Distance from route at which an observation is strongly suspicious. Initial test value; tune in HF. */
    val probableDeviationMeters: Double = 80.0,
    /** Number of consecutive suspicious observations needed before escalation. */
    val possibleDeviationSamples: Int = 2,
    val probableDeviationSamples: Int = 3,
    /** Poor accuracy must not by itself become a deviation. */
    val weakAccuracyMeters: Double = 50.0,
    /** Reject physically implausible route jumps instead of moving the walking state. Initial test value. */
    val maxPlausibleSpeedKmh: Double = 8.0
)

data class GpsTrackingState(
    val state: GpsState,
    val lastReliableObservation: GpsObservation? = null,
    val lastObservation: GpsObservation? = null,
    val consecutiveSuspiciousSamples: Int = 0,
    val consecutiveProbableSamples: Int = 0
)
