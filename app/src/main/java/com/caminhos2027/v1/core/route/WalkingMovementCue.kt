package com.caminhos2027.v1.core.route

/** Coarse movement relationship with the route direction, based on consecutive reliable route positions. */
enum class WalkingMovementCue {
    FORWARD,
    BACKWARD,
    STATIONARY,
    UNKNOWN
}

object WalkingMovementCueEvaluator {
    /**
     * Route-km is the ordered distance along the official route. A small tolerance avoids
     * turning GPS jitter into a visible direction change.
     */
    fun evaluate(previousRouteKm: Double?, currentRouteKm: Double?, toleranceKm: Double = 0.02): WalkingMovementCue {
        if (previousRouteKm == null || currentRouteKm == null ||
            !previousRouteKm.isFinite() || !currentRouteKm.isFinite() ||
            !toleranceKm.isFinite() || toleranceKm < 0.0
        ) {
            return WalkingMovementCue.UNKNOWN
        }

        val deltaKm = currentRouteKm - previousRouteKm
        return when {
            deltaKm > toleranceKm -> WalkingMovementCue.FORWARD
            deltaKm < -toleranceKm -> WalkingMovementCue.BACKWARD
            else -> WalkingMovementCue.STATIONARY
        }
    }
}
