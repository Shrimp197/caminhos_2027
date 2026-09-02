package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgress

/** Single read model for the active walking experience. UI consumes this state instead of rebuilding it. */
data class WalkingState(
    val walk: Walk,
    val routePosition: RoutePosition?,
    val gpsState: GpsState,
    val progress: WalkingProgress?,
    val nextApoi: Apoi?,
    /** Route distance from the current position to the next APOI, not absolute route km. */
    val nextApoiDistanceKm: Double?,
    val isOffline: Boolean = false
)
