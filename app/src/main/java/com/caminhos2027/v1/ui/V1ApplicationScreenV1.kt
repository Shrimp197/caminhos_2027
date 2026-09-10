package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import com.caminhos2027.v1.core.walking.WalkingState

/** Preparation owns the refined entry point; active walking uses the V2 acceptance surface. */
@androidx.compose.runtime.Composable
internal fun V1ApplicationScreenV1(
    state: WalkingState?,
    preparedWalk: Walk?,
    startRequested: Boolean,
    pendingStartDistanceMeters: Double?,
    appState: AppState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    surface: WalkingSurface,
    onPrepare: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onConfirmPreparation: (Double, Double, WalkingPreparationConfig) -> Unit,
    onStart: () -> Unit,
    onCancelPendingStart: () -> Unit,
    onStop: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onApoiSelected: (Apoi) -> Unit,
    onApoiScopeChanged: (Double?) -> Unit,
    onApoiSearchChanged: (String) -> Unit,
    onApoiFilterToggled: (ApoiCategory) -> Unit,
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit,
    onBackToWalking: () -> Unit,
    onBackToApoiBrowser: () -> Unit
) {
    if (surface == WalkingSurface.PREPARATION) {
        PreparationExperienceV2(route, routeOptions, selectedRouteId, onSelectRoute, onConfirmPreparation, onBackToWalking)
    } else if (surface == WalkingSurface.ACTIVE && state != null) {
        V1ActiveExperienceScreenV2(
            state = state,
            route = route,
            routeOptions = routeOptions,
            onStop = onStop,
            onOpenApoi = onOpenApoi,
            onOpenDecision = onOpenDecision,
            onQaAdvance = onQaAdvance,
            onQaToggleGps = onQaToggleGps,
            onQaDeviation = onQaDeviation
        )
    } else {
        V1PrimaryExperienceScreen(
            state = state,
            preparedWalk = preparedWalk,
            startRequested = startRequested,
            pendingStartDistanceMeters = pendingStartDistanceMeters,
            appState = appState,
            route = route,
            routeOptions = routeOptions,
            selectedRouteId = selectedRouteId,
            surface = surface,
            onPrepare = onPrepare,
            onSelectRoute = onSelectRoute,
            onConfirmPreparation = { start, destination -> onConfirmPreparation(start, destination, preparedWalk?.preparation ?: WalkingPreparationConfig()) },
            onStart = onStart,
            onCancelPendingStart = onCancelPendingStart,
            onStop = onStop,
            onOpenApoi = onOpenApoi,
            onOpenDecision = onOpenDecision,
            onApoiSelected = onApoiSelected,
            onApoiScopeChanged = onApoiScopeChanged,
            onApoiSearchChanged = onApoiSearchChanged,
            onApoiFilterToggled = onApoiFilterToggled,
            onQaAdvance = onQaAdvance,
            onQaToggleGps = onQaToggleGps,
            onQaDeviation = onQaDeviation,
            onBackToWalking = onBackToWalking,
            onBackToApoiBrowser = onBackToApoiBrowser
        )
    }
}