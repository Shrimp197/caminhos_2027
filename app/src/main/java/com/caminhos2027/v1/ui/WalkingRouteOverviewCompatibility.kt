package com.caminhos2027.v1.ui

import androidx.compose.runtime.Composable
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.walking.WalkingState

/** Adapters for callers that still provide aggregate route or walking state objects. */
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

@Composable
internal fun WalkingRouteProgressSurface(
    state: WalkingState,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    WalkingRouteProgressSurface(progress = state.progress)
}
