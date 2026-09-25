package com.caminhos2027.v1.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.caminhos2027.BuildConfig
import com.caminhos2027.v1.core.AndroidV1AppContainer
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.diary.DiaryEntry
import com.caminhos2027.v1.core.diary.DiaryRepository
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import com.caminhos2027.v1.core.route.RouteLocationEngine
import com.caminhos2027.v1.core.walking.WalkingState
import com.caminhos2027.v1.gps.GpxSimulationStartIndex
import com.caminhos2027.v1.core.walking.AndroidWalkingTrackingService
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.module.http.HttpRequestUtil
import java.time.Instant

class V1MainActivity : ComponentActivity() {
    private companion object { const val TAG = "CaminhosV1" }
    // V1 final delivery flow: keep production/QA boundaries explicit.
    private lateinit var appContainer: AndroidV1AppContainer
    private lateinit var diaryRepository: DiaryRepository
    private var diaryEntries by mutableStateOf<List<DiaryEntry>>(emptyList())
    private var diaryPhotoUri by mutableStateOf<String?>(null)
    private var trackingBinder: AndroidWalkingTrackingService.TrackingBinder? = null
    private var trackingBound = false
    private var trackingBindRequested = false
    private val walkingReconcileHandler = Handler(Looper.getMainLooper())
    private var walkingReconcileRunnable: Runnable? = null

    private val trackingListener = object : AndroidWalkingTrackingService.Listener {
        override fun onTrackingStateChanged(state: WalkingState?, pendingStart: Boolean, pendingDistanceMeters: Double?) {
            runOnUiThread {
                val savedPlanId = preparedWalk?.id
                val currentWalkId = walkingState?.walk?.id
                val activeState = state?.takeIf { it.walk.status == WalkStatus.ACTIVE }
                val belongsToCurrentPlan = activeState != null && (
                    savedPlanId == null ||
                        activeState.walk.id == savedPlanId ||
                        activeState.walk.id == currentWalkId
                    )

                if (activeState != null && belongsToCurrentPlan) {
                    walkingState = activeState
                    preparedWalk = null
                    startRequested = pendingStart
                    pendingStartDistanceMeters = pendingDistanceMeters
                    appContainer.store.setWalking(activeState)
                } else if (state == null && walkingState == null && preparedWalk == null) {
                    // A bound-only Service instance can legitimately have no in-memory container after
                    // Activity/process recreation of a paused walk. Keep any state already restored from
                    // persistence until the long-lived Service publishes its authoritative state.
                    walkingState = null
                    startRequested = pendingStart
                    pendingStartDistanceMeters = pendingDistanceMeters
                    appContainer.store.setWalking(null)
                }
            }
        }

        override fun onTrackingError(message: String) {
            runOnUiThread { pendingStartDistanceMeters = null }
        }
    }

    private val trackingConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (!trackingBindRequested) {
                return
            }
            trackingBinder = service as AndroidWalkingTrackingService.TrackingBinder
            trackingBound = true
            trackingBinder?.register(trackingListener)
            if (walkingState?.walk?.status == WalkStatus.ACTIVE && walkingState?.isPaused == false) {
                trackingBinder?.startTracking()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            trackingBinder = null
            trackingBound = false
        }
    }
    private var walkingState by mutableStateOf<WalkingState?>(null)
    private var preparedWalk by mutableStateOf<Walk?>(null)
    private var startRequested by mutableStateOf(false)
    private var pendingStartDistanceMeters by mutableStateOf<Double?>(null)
    private var surface by mutableStateOf(WalkingSurface.ACTIVE)
    private var selectedRouteId by mutableStateOf(AndroidRouteCatalog.CENTENARIO_ID)
    private var pilgrimModeOnStart by mutableStateOf(false)

    private val diaryPhotoLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        diaryPhotoUri = uri.toString()
        surface = WalkingSurface.DIARY
    }

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (hasLocationPermissionAfterResult(permissions)) startTrackingService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        HttpRequestUtil.setOkHttpClient(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Caminhos-do-Peregrino/1.2.0 Android")
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        val persistedRouteId = AndroidRouteCatalog.preferredPersistedRouteId(this)
        appContainer = AndroidV1AppContainer(this, persistedRouteId ?: AndroidRouteCatalog.CENTENARIO_ID)
        diaryRepository = DiaryRepository(this)
        diaryEntries = diaryRepository.load()
        selectedRouteId = appContainer.publishedRoute().id
        pilgrimModeOnStart = getSharedPreferences("peregrino_preferences", MODE_PRIVATE).getBoolean("pilgrim_mode_on_start", false)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    startRequested -> cancelPendingStart()
                    surface == WalkingSurface.ACTIVE && walkingState == null && preparedWalk != null -> surface = WalkingSurface.PREPARATION
                    surface == WalkingSurface.ACTIVE -> finish()
                    surface == WalkingSurface.PREPARATION -> finish()
                    surface == WalkingSurface.APOI_BROWSER -> returnToWalking()
                    surface == WalkingSurface.APOI_DETAIL -> returnToApoiBrowser()
                    surface == WalkingSurface.DECISION -> returnToWalking()
                    surface == WalkingSurface.SETTINGS -> openMore()
                    surface == WalkingSurface.PREPARATION -> finish()
                    surface == WalkingSurface.ACTIVE -> finish()
                    else -> returnToWalking()
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
                    onTogglePause = ::togglePause,
                    onOpenApoi = ::openApoiBrowser,
                    onOpenNext10Km = ::openNext10Km,
                    onOpenDecision = ::openDecision,
                    onOpenSummary = ::openSummary,
                    onOpenMap = ::openMap,
                    onOpenDiary = ::openDiary,
                    onOpenMore = ::openMore,
                    onOpenSos = ::openSos,
                    onOpenSmartwatch = ::openSmartwatch,
                    onOpenPilgrimMode = ::openPilgrimMode,
                    onExitPilgrimMode = ::exitPilgrimMode,
                    onOpenSettings = ::openSettings,
                    pilgrimModeOnStart = pilgrimModeOnStart,
                    onTogglePilgrimModeOnStart = ::updatePilgrimModeOnStart,
                    onOpenNotificationSettings = ::openNotificationSettings,
                    onNavigate = ::navigate,
                    diaryEntries = diaryEntries,
                    diaryPhotoUri = diaryPhotoUri,
                    onDiaryChoosePhoto = ::chooseDiaryPhoto,
                    onDiaryClearPhoto = { diaryPhotoUri = null },
                    onDiaryAdd = ::addDiaryEntry,
                    onInfo = ::showInfo,
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
        bindTrackingService()
        if (startRequested || (walkingState != null && !walkingState!!.isPaused)) {
            if (hasLocationPermission()) startTrackingService() else requestLocationPermission()
        }
        if (startRequested) scheduleWalkingStateReconciliation()
    }

    override fun onStop() {
        disconnectTrackingService()
        super.onStop()
    }

    override fun onDestroy() {
        cancelWalkingStateReconciliation()
        disconnectTrackingService()
        super.onDestroy()
    }

    private fun disconnectTrackingService() {
        trackingBinder?.unregister(trackingListener)
        if (trackingBindRequested) {
            runCatching { unbindService(trackingConnection) }
        }
        trackingBindRequested = false
        trackingBound = false
        trackingBinder = null
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
            surface = if (pilgrimModeOnStart) WalkingSurface.PILGRIM_MODE else WalkingSurface.ACTIVE
            return
        }
        preparedWalk = appContainer.restorePreparedWalk()?.walk
        preparedWalk?.let { saved ->
            selectedRouteId = saved.routeId
        }
        walkingState = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = if (preparedWalk == null) WalkingSurface.PREPARATION else WalkingSurface.ACTIVE
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
        if (BuildConfig.DEBUG && isTestRoute()) {
            startQaPreparedWalk()
        } else {
            scheduleWalkingStateReconciliation()
            if (hasLocationPermission()) startTrackingService() else requestLocationPermission()
        }
    }

    /**
     * QA-only deterministic start: the debug GPX track supplies the first raw point, but the
     * application still goes through the normal preparation controller and persistent runtime.
     * Production routes always wait for the real Android GPS callback.
     */
    private fun startQaPreparedWalk() {
        val saved = preparedWalk ?: run {
            Log.e(TAG, "QA start ignored: preparedWalk is null")
            return
        }
        val route = appContainer.publishedRoute()
        val startKm = saved.plannedStartKm ?: 0.0
        val index = GpxSimulationStartIndex.nearestPointIndex(route, startKm)
        val point = route.geometry.points[index]
        val raw = RawGpsPosition(
            latitude = point.latitude,
            longitude = point.longitude,
            accuracyMeters = 1.0,
            capturedAt = Instant.now()
        )
        val position = RouteLocationEngine.locate(route, raw)
        Log.i(TAG, "QA start: route=" + route.id + ", plannedStartKm=" + startKm + ", index=" + index + ", positionKm=" + position.routeKm + ", distance=" + position.distanceToRouteMeters)
        val started = runCatching {
            appContainer.preparationController.startSaved(
                catalog = appContainer.publishedApoiCatalog(),
                position = position,
                now = raw.capturedAt
            )
        }.getOrElse {
            pendingStartDistanceMeters = position.distanceToRouteMeters
            startRequested = false
            Log.e(TAG, "QA start failed", it)
            showInfo("Não foi possível iniciar a caminhada de teste: " + (it.message ?: "erro desconhecido"))
            return
        }
        val walking = started.walking ?: return
        appContainer.attachWalk(walking.walk)
        appContainer.store.setWalking(walking)
        walkingState = walking
        preparedWalk = null
        startRequested = false
        pendingStartDistanceMeters = null
        surface = if (pilgrimModeOnStart) WalkingSurface.PILGRIM_MODE else WalkingSurface.ACTIVE
        Log.i(TAG, "QA start succeeded: walk=" + walking.walk.id + ", status=" + walking.walk.status)
        if (hasLocationPermission()) startTrackingService()
    }

    private fun cancelPendingStart() {
        startRequested = false
        pendingStartDistanceMeters = null
        cancelWalkingStateReconciliation()
        trackingBinder?.cancelPendingStart()
        surface = WalkingSurface.ACTIVE
    }

    /**
     * Closes the Activity/foreground-service callback race: the service persists an active walk
     * before the binder callback necessarily reaches a newly created Activity.
     */
    private fun scheduleWalkingStateReconciliation() {
        cancelWalkingStateReconciliation()
        var attempts = 0
        val runnable = object : Runnable {
            override fun run() {
                if (!startRequested) return
                val restored = runCatching { appContainer.resumePersistedWalk().walking }.getOrNull()
                val active = restored?.takeIf { it.walk.status == WalkStatus.ACTIVE }
                if (active != null) {
                    walkingState = active
                    preparedWalk = null
                    startRequested = false
                    pendingStartDistanceMeters = null
                    surface = if (pilgrimModeOnStart) WalkingSurface.PILGRIM_MODE else WalkingSurface.ACTIVE
                    return
                }
                attempts += 1
                if (attempts < 24) walkingReconcileHandler.postDelayed(this, 500L)
            }
        }
        walkingReconcileRunnable = runnable
        walkingReconcileHandler.post(runnable)
    }

    private fun cancelWalkingStateReconciliation() {
        walkingReconcileRunnable?.let(walkingReconcileHandler::removeCallbacks)
        walkingReconcileRunnable = null
    }

    private fun isTestRoute(): Boolean = AndroidRouteCatalog.isTestRoute(selectedRouteId)

    private fun startTrackingService() {
        val intent = Intent(this, AndroidWalkingTrackingService::class.java).apply {
            action = AndroidWalkingTrackingService.ACTION_START
            putExtra(AndroidWalkingTrackingService.EXTRA_ROUTE_ID, selectedRouteId)
        }
        androidx.core.content.ContextCompat.startForegroundService(this, intent)
        bindTrackingService()
    }

    private fun bindTrackingService() {
        if (trackingBindRequested) return
        trackingBindRequested = bindService(
            Intent(this, AndroidWalkingTrackingService::class.java),
            trackingConnection,
            0
        )
    }

    private fun togglePause() {
        if (walkingState?.isPaused == true) {
            // Pausing intentionally stops the started service. After Activity/process recreation the
            // existing binder may belong to a newly bound, non-started Service instance with no container.
            // Always restart through the foreground-service entry point so persisted paused state is restored
            // and the resume transition is executed by the long-lived service.
            startTrackingService()
        } else {
            trackingBinder?.pauseWalking()
        }
    }

    private fun postWhenTrackingBinderReady(
        action: (AndroidWalkingTrackingService.TrackingBinder) -> Unit,
        attemptsRemaining: Int = 20
    ) {
        val binder = trackingBinder
        if (binder != null) {
            action(binder)
        } else if (attemptsRemaining > 0) {
            walkingReconcileHandler.postDelayed(
                { postWhenTrackingBinderReady(action, attemptsRemaining - 1) },
                100L
            )
        }
    }

    private fun qaAdvance() {
        postWhenTrackingBinderReady(action = { it.qaAdvance() })
    }

    private fun qaSetGpsAvailability(available: Boolean) {
        postWhenTrackingBinderReady(action = { it.qaSetGpsAvailability(available) })
    }

    private fun qaSimulateDeviation() {
        Log.i(TAG, "QA deviation button invoked; binderReady=" + (trackingBinder != null))
        postWhenTrackingBinderReady(action = {
            Log.i(TAG, "QA deviation forwarded to tracking binder")
            it.qaSimulateDeviation()
        })
    }

    private fun openApoiBrowser() {
        if (walkingState?.routePosition == null) return
        val query = appContainer.store.state.apoiBrowser?.query
        appContainer.apoiDecisionController.browseApoi(
            text = query?.text ?: "",
            filter = query?.filter ?: ApoiFilter(),
            limit = 50,
            maxDistanceKm = null
        )
        appContainer.apoiDecisionController.clearDecision()
        surface = WalkingSurface.APOI_BROWSER
    }

    private fun openNext10Km() {
        if (walkingState?.routePosition == null) return
        val query = appContainer.store.state.apoiBrowser?.query
        appContainer.apoiDecisionController.browseApoi(
            text = query?.text ?: "",
            filter = query?.filter ?: ApoiFilter(),
            limit = 50,
            maxDistanceKm = 10.0
        )
        appContainer.apoiDecisionController.clearDecision()
        surface = WalkingSurface.NEXT_10_KM
    }

    private fun openSummary() { surface = WalkingSurface.SUMMARY }
    private fun openMap() { surface = WalkingSurface.ACTIVE }
    private fun openDiary() { surface = WalkingSurface.DIARY }
    private fun openMore() { surface = WalkingSurface.MORE }
    private fun openSos() {
        val query = appContainer.store.state.apoiBrowser?.query
        val emergencyFilter = (query?.filter ?: ApoiFilter()).copy(
            services = setOf(ApoiCategory.EMERGENCIA)
        )
        if (walkingState?.routePosition != null) {
            appContainer.apoiDecisionController.browseApoi(
                text = "",
                filter = emergencyFilter,
                limit = 20,
                maxDistanceKm = null
            )
        }
        surface = WalkingSurface.SOS
    }
    private fun openSmartwatch() { surface = WalkingSurface.SMARTWATCH }

    private fun openSettings() { surface = WalkingSurface.SETTINGS }

    private fun updatePilgrimModeOnStart(enabled: Boolean) {
        pilgrimModeOnStart = enabled
        getSharedPreferences("peregrino_preferences", MODE_PRIVATE)
            .edit()
            .putBoolean("pilgrim_mode_on_start", enabled)
            .apply()
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    private fun openPilgrimMode() {
        if (walkingState == null) return
        getSharedPreferences("peregrino_preferences", MODE_PRIVATE).edit().putBoolean("pilgrim_mode", true).apply()
        surface = WalkingSurface.PILGRIM_MODE
    }

    private fun exitPilgrimMode() {
        getSharedPreferences("peregrino_preferences", MODE_PRIVATE).edit().putBoolean("pilgrim_mode", false).apply()
        surface = WalkingSurface.ACTIVE
    }

    private fun pilgrimModeEnabled(): Boolean =
        getSharedPreferences("peregrino_preferences", MODE_PRIVATE).getBoolean("pilgrim_mode", false)

    private fun navigate(destination: WalkingSurface) {
        when (destination) {
            WalkingSurface.ACTIVE -> openMap()
            WalkingSurface.SUMMARY -> openSummary()
            WalkingSurface.APOI_BROWSER -> openApoiBrowser()
            WalkingSurface.DIARY -> openDiary()
            WalkingSurface.MORE -> openMore()
            WalkingSurface.SETTINGS -> openSettings()
            WalkingSurface.SOS -> openSos()
            WalkingSurface.SMARTWATCH -> openSmartwatch()
            WalkingSurface.PILGRIM_MODE -> openPilgrimMode()
            WalkingSurface.NEXT_10_KM -> openNext10Km()
            else -> Unit
        }
    }

    private fun chooseDiaryPhoto() { diaryPhotoLauncher.launch(arrayOf("image/*")) }

    private fun addDiaryEntry(text: String) {
        val state = walkingState
        val position = state?.routePosition
        diaryEntries = diaryRepository.add(
            DiaryEntry(
                content = text,
                walkId = state?.walk?.id,
                routeKm = position?.routeKm,
                location = position?.projectedPoint,
                photoUri = diaryPhotoUri
            )
        )
        diaryPhotoUri = null
    }

    private fun showInfo(message: String) {
        android.app.AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
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
        trackingBinder?.stopWalking()
        // A Service can remain alive while bound even after stopSelf(). Detach the Activity so the next
        // explicit start receives a fresh service/container instance rather than a stopped one.
        disconnectTrackingService()
        walkingState = null
        preparedWalk = null
        startRequested = false
        pendingStartDistanceMeters = null
        getSharedPreferences("peregrino_preferences", MODE_PRIVATE).edit().putBoolean("pilgrim_mode", false).apply()
        surface = WalkingSurface.PREPARATION
    }
}
