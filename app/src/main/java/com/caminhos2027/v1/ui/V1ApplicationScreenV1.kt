package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.diary.DiaryEntry
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import com.caminhos2027.v1.core.walking.WalkingState

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
    diaryEntries: List<DiaryEntry>,
    diaryPhotoUri: String?,
    onPrepare: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onConfirmPreparation: (Double, Double, WalkingPreparationConfig) -> Unit,
    onStart: () -> Unit,
    onCancelPendingStart: () -> Unit,
    onStop: () -> Unit,
    onTogglePause: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenNext10Km: () -> Unit,
    onOpenDecision: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenMore: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenSmartwatch: () -> Unit,
    onOpenPilgrimMode: () -> Unit,
    onExitPilgrimMode: () -> Unit,
    onNavigate: (WalkingSurface) -> Unit,
    onDiaryChoosePhoto: () -> Unit,
    onDiaryClearPhoto: () -> Unit,
    onDiaryAdd: (String) -> Unit,
    onApoiSelected: (Apoi) -> Unit,
    onApoiScopeChanged: (Double?) -> Unit,
    onApoiSearchChanged: (String) -> Unit,
    onApoiFilterToggled: (ApoiCategory) -> Unit,
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit,
    onBackToWalking: () -> Unit,
    onBackToApoiBrowser: () -> Unit,
    onInfo: (String) -> Unit
) {
    when {
        surface == WalkingSurface.PREPARATION ->
            PreparationExperienceV3(route, routeOptions, selectedRouteId, onSelectRoute, onConfirmPreparation, onBackToWalking)

        surface == WalkingSurface.ACTIVE && state != null ->
            V1ActiveExperienceScreenV2(
                state = state,
                route = route,
                routeOptions = routeOptions,
                onStop = onStop,
                onTogglePause = onTogglePause,
                onOpenApoi = onOpenApoi,
                onOpenDecision = onOpenDecision,
                onOpenNext10Km = onOpenNext10Km,
                onOpenSummary = onOpenSummary,
                onOpenDiary = onOpenDiary,
                onOpenMore = onOpenMore,
                onOpenSos = onOpenSos,
                onOpenPilgrimMode = onOpenPilgrimMode,
                onNavigate = onNavigate,
                onQaAdvance = onQaAdvance,
                onQaToggleGps = onQaToggleGps,
                onQaDeviation = onQaDeviation
            )

        surface == WalkingSurface.SUMMARY ->
            SummarySurfaceV1(state, route, onOpenMap, onOpenApoi, onOpenDiary, onOpenMore)

        surface == WalkingSurface.NEXT_10_KM ->
            Next10KmSurfaceV1(state, appState.apoiBrowser?.results ?: emptyList(), onOpenMap, onNavigate)

        surface == WalkingSurface.DIARY ->
            DiarySurfaceV1(diaryEntries, state, diaryPhotoUri, onDiaryChoosePhoto, onDiaryClearPhoto, onDiaryAdd, onNavigate)

        surface == WalkingSurface.SOS ->
            SosSurfaceV1(state, onNavigate)

        surface == WalkingSurface.SMARTWATCH ->
            SmartwatchSurfaceV1(onNavigate, onOpenMap)

        surface == WalkingSurface.PILGRIM_MODE && state != null ->
            PilgrimModeSurfaceV1(state, onOpenApoi, onOpenSos, onExitPilgrimMode)

        surface == WalkingSurface.MORE ->
            MoreSurfaceV1(
                onPrepare = onPrepare,
                onMap = onOpenMap,
                onApoi = onOpenApoi,
                onDiary = onOpenDiary,
                onHelp = { onInfo("Ajuda: prepare o percurso, guarde o plano, inicie quando tiver GPS válido e use Mapa, Apoios, Diário e Mais durante a caminhada.") },
                onContact = { onInfo("Contacto: utilize a aplicação de comunicação disponível no dispositivo.") },
                onAbout = { onInfo("Caminhos do Peregrino · versão Android · dados publicados separados de QA.") },
                onSmartwatch = onOpenSmartwatch,
                onSos = onOpenSos,
                onPilgrimMode = onOpenPilgrimMode,
                onNavigate = onNavigate
            )

        surface == WalkingSurface.APOI_BROWSER || surface == WalkingSurface.APOI_DETAIL || surface == WalkingSurface.DECISION ->
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
                onConfirmPreparation = { start, destination ->
                    onConfirmPreparation(start, destination, preparedWalk?.preparation ?: WalkingPreparationConfig())
                },
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

        else ->
            V1PrimaryExperienceScreen(
                state = state,
                preparedWalk = preparedWalk,
                startRequested = startRequested,
                pendingStartDistanceMeters = pendingStartDistanceMeters,
                appState = appState,
                route = route,
                routeOptions = routeOptions,
                selectedRouteId = selectedRouteId,
                surface = WalkingSurface.ACTIVE,
                onPrepare = onPrepare,
                onSelectRoute = onSelectRoute,
                onConfirmPreparation = { start, destination ->
                    onConfirmPreparation(start, destination, preparedWalk?.preparation ?: WalkingPreparationConfig())
                },
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
