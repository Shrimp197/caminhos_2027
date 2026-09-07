package com.caminhos2027.v1.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.caminhos2027.v1.core.AndroidV1AppContainer
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import com.caminhos2027.v1.core.route.RouteLocationEngine
import com.caminhos2027.v1.core.walking.WalkingState
import com.caminhos2027.v1.gps.AndroidLocationSource
import com.caminhos2027.v1.gps.GpxSimulationLocationSource
import com.caminhos2027.v1.gps.GpxSimulationStartIndex
import com.caminhos2027.v1.gps.LocationSource
import java.time.Instant

class V1MainActivity : ComponentActivity() {
    private lateinit var appContainer: AndroidV1AppContainer
    private var locationSource: LocationSource? = null
    private var testLocationSource: GpxSimulationLocationSource? = null
    private var walkingState by mutableStateOf<WalkingState?>(null)
    private var preparedWalk by mutableStateOf<Walk?>(null)
    private var startRequested by mutableStateOf(false)
    private var pendingStartDistanceMeters by mutableStateOf<Double?>(null)
    private var surface by mutableStateOf(WalkingSurface.ACTIVE)
    private var selectedRouteId by mutableStateOf(AndroidRouteCatalog.CENTENARIO_ID)

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (hasLocationPermissionAfterResult(permissions)) startWalkingLocationSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AndroidV1AppContainer(this)
        selectedRouteId = appContainer.publishedRoute().id
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    startRequested -> cancelPendingStart()
                    surface == WalkingSurface.ACTIVE && walkingState == null && preparedWalk != null -> surface = WalkingSurface.PREPARATION
                    surface == WalkingSurface.ACTIVE -> finish()
                    surface == WalkingSurface.PREPARATION -> surface = WalkingSurface.ACTIVE
                    surface == WalkingSurface.APOI_BROWSER -> returnToWalking()
                    surface == WalkingSurface.APOI_DETAIL -> returnToApoiBrowser()
                    surface == WalkingSurface.DECISION -> returnToWalking()
                }
            }
        })
        setContent {
            CaminhosTheme {
                V1ApplicationScreenV1(
                    state = walkingState,
                    preparedWalk = preparedWalk,
                    startRequested = startRequested,
                    pendingStartDistanceMeters = pendingStartDistanceMeters,
                    appState = appContainer.store.state,
                    route = appContainer.publishedRoute(),
                    routeOptions = AndroidRouteCatalog.options,
                    selectedRouteId = selectedRouteId,
                    surface = surface,
                    onPrepare = ::openPreparation,
                    onSelectRoute = ::selectRoute,
                    onConfirmPreparation = ::prepareSelectedWalkingWithConfig,
                    onStart = ::requestStartPreparedWalk,
                    onCancelPendingStart = ::cancelPendingStart,
                    onStop = ::stopWalking,
                    onOpenApoi = ::openApoiBrowser,
                    onOpenDecision = ::openDecision,
                    onApoiSelected = ::selectApoi,
                    onApoiScopeChanged = ::updateApoiScope,
                    onApoiSearchChanged = ::updateApoiSearch,
                    onApoiFilterToggled = ::toggleApoiFilter,
                    onQaAdvance = ::qaAdvance,
                    onQaToggleGps = ::qaSetGpsAvailability,
                    onQaDeviation = ::qaSimulateDeviation,
                    onBackToWalking = ::returnToWalking,
                    onBackToApoiBrowser = ::returnToApoiBrowser
                )
            }
        }
        restoreWalkingSession()
    }

    override fun onStart() {
        super.onStart()
        if (walkingState != null || startRequested) {
            if (isTestRoute()) startTestRouteIfNeeded()
            else if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
        }
    }

    override fun onStop() {
        locationSource?.stop()
        locationSource = null
        testLocationSource = null
        super.onStop()
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermissionAfterResult(permissions: Map<String, Boolean>): Boolean =
        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun restoreWalkingSession() {
        val restored = appContainer.resumePersistedWalk()
        val state = restored.walking
        if (state != null) {
            walkingState = state
            selectedRouteId = state.walk.routeId
            preparedWalk = null
            startRequested = false
            pendingStartDistanceMeters = null
            surface = WalkingSurface.ACTIVE
            return
        }
        preparedWalk = appContainer.restorePreparedWalk()?.walk
        preparedWalk?.let { selectedRouteId = it.routeId }
        walkingState = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = WalkingSurface.ACTIVE
    }

    private fun openPreparation() {
        if (walkingState != null || startRequested) return
        surface = WalkingSurface.PREPARATION
    }

    private fun selectRoute(routeId: String) {
        if (walkingState != null || startRequested) return
        appContainer = AndroidV1AppContainer(this, routeId)
        selectedRouteId = routeId
        preparedWalk = appContainer.restorePreparedWalk()?.walk
        walkingState = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = WalkingSurface.PREPARATION
    }

    private fun prepareSelectedWalkingWithConfig(
        startRouteKm: Double,
        destinationRouteKm: Double,
        preparation: WalkingPreparationConfig
    ) {
        val prepared = appContainer.preparationController.save(
            walkId = "walk-${System.currentTimeMillis()}",
            startRouteKm = startRouteKm,
            destinationRouteKm = destinationRouteKm,
            preparation = preparation
        )
        preparedWalk = prepared.walking?.walk
        walkingState = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = WalkingSurface.ACTIVE
    }

    private fun requestStartPreparedWalk() {
        require(preparedWalk?.status == WalkStatus.PLANNED) { "A planned walk is required before starting" }
        startRequested = true
        pendingStartDistanceMeters = null
        if (isTestRoute()) startTestRouteIfNeeded()
        else if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
    }

    private fun cancelPendingStart() {
        startRequested = false
        pendingStartDistanceMeters = null
        locationSource?.stop()
        locationSource = null
        testLocationSource = null
        surface = WalkingSurface.ACTIVE
    }

    private fun isTestRoute(): Boolean = AndroidRouteCatalog.isTestRoute(selectedRouteId)

    private fun startTestRouteIfNeeded() {
        if (testLocationSource != null) return
        if (walkingState == null && (!startRequested || preparedWalk == null)) return

        val route = appContainer.publishedRoute()
        val plannedStartKm = preparedWalk?.plannedStartKm ?: 0.0
        val source = GpxSimulationLocationSource(
            points = route.geometry.points,
            onPosition = { position ->
                runOnUiThread {
                    if (!handleGpsForPreparedWalk(position) && walkingState != null) {
                        walkingState = appContainer.activeController().acceptGps(position).walking
                    }
                }
            },
            onAvailabilityChanged = { available ->
                if (!available && walkingState != null) {
                    walkingState = appContainer.activeController().markNoSignal(Instant.now()).walking
                }
            },
            initialIndex = GpxSimulationStartIndex.nearestPointIndex(route, plannedStartKm)
        )
        testLocationSource = source
        locationSource = source
        source.start()
    }

    private fun handleGpsForPreparedWalk(position: RawGpsPosition): Boolean {
        if (!startRequested || walkingState != null || preparedWalk == null) return false
        val routePosition = RouteLocationEngine.locate(appContainer.publishedRoute(), position)
        pendingStartDistanceMeters = routePosition.distanceToRouteMeters.takeIf { it.isFinite() }
        val started = try {
            appContainer.preparationController.startSaved(
                catalog = appContainer.publishedApoiCatalog(),
                position = routePosition,
                now = position.capturedAt
            )
        } catch (_: IllegalArgumentException) {
            return false
        }
        val walking = started.walking ?: return false
        appContainer.attachWalk(walking.walk)
        appContainer.store.setWalking(walking)
        walkingState = walking
        preparedWalk = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = WalkingSurface.ACTIVE
        return true
    }

    private fun startWalkingLocationSource() {
        if (locationSource != null || (walkingState == null && !startRequested)) return
        val source = AndroidLocationSource(
            context = this,
            onPosition = { position ->
                runOnUiThread {
                    if (!handleGpsForPreparedWalk(position) && walkingState != null) {
                        walkingState = appContainer.activeController().acceptGps(position).walking
                    }
                }
            },
            onAvailabilityChanged = { available ->
                if (!available) runOnUiThread {
                    if (walkingState != null) walkingState = appContainer.activeController().markNoSignal(Instant.now()).walking
                }
            }
        )
        locationSource = source
        source.start()
    }

    private fun qaAdvance() { testLocationSource?.advance() }
    private fun qaSetGpsAvailability(available: Boolean) { testLocationSource?.setAvailable(available) }
    private fun qaSimulateDeviation() { testLocationSource?.simulateDeviation() }

    private fun openApoiBrowser() {
        if (walkingState?.routePosition == null) return
        val query = appContainer.store.state.apoiBrowser?.query
        appContainer.apoiDecisionController.browseApoi(
            text = query?.text ?: "",
            filter = query?.filter ?: ApoiFilter(),
            limit = if (query?.maxDistanceKm == null) 50 else 8,
            maxDistanceKm = 10.0
        )
        appContainer.apoiDecisionController.clearDecision()
        surface = WalkingSurface.APOI_BROWSER
    }

    private fun updateApoiScope(maxDistanceKm: Double?) {
        val query = appContainer.store.state.apoiBrowser?.query ?: return
        appContainer.apoiDecisionController.browseApoi(query.text, query.filter, if (maxDistanceKm == null) 50 else 8, maxDistanceKm)
    }

    private fun updateApoiSearch(text: String) {
        val query = appContainer.store.state.apoiBrowser?.query ?: return
        appContainer.apoiDecisionController.browseApoi(text, query.filter, if (query.maxDistanceKm == null) 50 else 8, query.maxDistanceKm)
    }

    private fun toggleApoiFilter(category: ApoiCategory) {
        val query = appContainer.store.state.apoiBrowser?.query ?: return
        val services = query.filter.services.toMutableSet().apply { if (!add(category)) remove(category) }
        appContainer.apoiDecisionController.browseApoi(query.text, query.filter.copy(services = services), if (query.maxDistanceKm == null) 50 else 8, query.maxDistanceKm)
    }

    private fun selectApoi(apoi: com.caminhos2027.v1.core.model.Apoi) {
        appContainer.apoiDecisionController.selectApoi(apoi.id)
        surface = WalkingSurface.APOI_DETAIL
    }

    private fun openDecision() {
        if (walkingState?.routePosition == null) return
        appContainer.apoiDecisionController.clearApoiSelection()
        appContainer.apoiDecisionController.buildDecision()
        surface = WalkingSurface.DECISION
    }

    private fun returnToWalking() {
        appContainer.apoiDecisionController.clearApoiSelection()
        appContainer.apoiDecisionController.clearDecision()
        surface = WalkingSurface.ACTIVE
    }

    private fun returnToApoiBrowser() {
        appContainer.apoiDecisionController.clearApoiSelection()
        surface = WalkingSurface.APOI_BROWSER
    }

    private fun stopWalking() {
        val position = walkingState?.routePosition ?: return
        appContainer.runtime.stop(position, Instant.now())
        appContainer.clearSession()
        locationSource?.stop()
        locationSource = null
        testLocationSource = null
        walkingState = null
        preparedWalk = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = WalkingSurface.ACTIVE
    }
}
