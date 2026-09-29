package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint

/** Chooses the physical point a pilgrim should be guided back to when starting/rejoining a walk. */
object WalkingGuidanceTargetPolicy {
    data class Target(val point: GeoPoint, val label: String)

    fun forPendingStart(
        currentDistanceToRouteMeters: Double,
        possibleDeviationMeters: Double,
        plannedStart: GeoPoint,
        routeName: String
    ): Target? =
        if (currentDistanceToRouteMeters >= possibleDeviationMeters) {
            Target(plannedStart, "Início da caminhada · $routeName")
        } else {
            null
        }

    fun forActiveWalk(
        currentDistanceToRouteMeters: Double,
        possibleDeviationMeters: Double,
        lastKnownOnRoute: GeoPoint?
    ): Target? =
        if (currentDistanceToRouteMeters >= possibleDeviationMeters && lastKnownOnRoute != null) {
            Target(lastKnownOnRoute, "Regressar ao último ponto conhecido no Caminho")
        } else {
            null
        }
}
