package com.caminhos2027.v1.ui

import androidx.compose.runtime.Composable
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.walking.WalkingState

/** Adapter for callers that still provide the aggregate route and walking state. */
@Composable
internal fun WalkingRouteOverviewSurface(
    route: Route,
    state: WalkingState,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    WalkingRouteOverviewSurface(
        geometry = route.geometry.points,
        currentRouteKm = state.routePosition?.routeKm,
        totalDistanceKm = route.totalDistanceKm,
        modifier = modifier
    )
}
