package com.caminhos2027.v1.core.walking

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.caminhos2027.R
import com.caminhos2027.v1.core.AndroidV1AppContainer
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.RouteLocationEngine
import com.caminhos2027.v1.gps.AndroidLocationSource
import com.caminhos2027.v1.gps.GpxSimulationLocationSource
import com.caminhos2027.v1.gps.GpxSimulationStartIndex
import com.caminhos2027.v1.gps.LocationSource
import java.time.Instant

/** Owns long-lived walking GPS collection independently of the Activity lifecycle. */
class AndroidWalkingTrackingService : Service() {
    interface Listener {
        fun onTrackingStateChanged(state: WalkingState?, pendingStart: Boolean, pendingDistanceMeters: Double?)
        fun onTrackingError(message: String)
        fun onStartGuidanceNeeded(target: GeoPoint, label: String) {}
    }

    inner class TrackingBinder : Binder() {
        fun register(listener: Listener) {
            listeners += listener
            listener.onTrackingStateChanged(walkingState, pendingStart, pendingStartDistanceMeters)
        }
        fun unregister(listener: Listener) { listeners -= listener }
        fun startTracking() = postWhenContainerReady(action = { beginTracking() })
        fun pauseWalking() = postWhenContainerReady(action = { pause() })
        fun resumeWalking() = postWhenWalkingStateReady(action = { beginTracking() })
        fun stopWalking() = stopWalkingSession()
        fun cancelPendingStart() = cancelPendingStartInternal()
        fun qaAdvance() {
            postWhenSimulationReady(action = { source ->
                val before = source.currentIndex
                val moved = source.advance(150.0)
                Log.i(TAG, "QA advance: started=" + source.isStarted + ", index=" + before + "->" + source.currentIndex + ", moved=" + moved)
            })
        }
        fun qaSetGpsAvailability(available: Boolean) {
            postWhenSimulationReady(action = { source ->
                source.setAvailable(available)
                // Recovery QA must exercise the same raw-position path as real GPS.
                // Provider availability alone is not a GPS fix; force one fresh raw fix.
                if (available) {
                    source.emitRecoveryFix()
                }
            })
        }
        fun qaSimulateDeviation() {
            postWhenSimulationReady(action = { source ->
                Log.i(TAG, "QA deviation command: route=" + routeId + ", sourceReady=true")
                source.simulateDeviation()
                source.simulateDeviation()
            })
        }
    }

    private val binder = TrackingBinder()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = linkedSetOf<Listener>()
    private var container: AndroidV1AppContainer? = null
    private var locationSource: LocationSource? = null
    private var simulationSource: GpxSimulationLocationSource? = null
    private var walkingState: WalkingState? = null
    private var pendingStart = false
    private var pendingStartDistanceMeters: Double? = null
    private var startGuidanceIssued = false
    private var routeId: String? = null
    private lateinit var eventNotifier: WalkingEventNotifier

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        eventNotifier = WalkingEventNotifier(this)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val requestedRouteId = intent?.getStringExtra(EXTRA_ROUTE_ID)
        if (requestedRouteId != null && requestedRouteId != routeId) {
            routeId = requestedRouteId
            container = AndroidV1AppContainer(this, requestedRouteId)
            walkingState = container?.resumePersistedWalk()?.walking
        } else if (container == null) {
            routeId = requestedRouteId
                ?: AndroidRouteCatalog.preferredPersistedRouteId(this)
                ?: AndroidRouteCatalog.CENTENARIO_ID
            container = AndroidV1AppContainer(this, routeId)
            walkingState = container?.resumePersistedWalk()?.walking
        }

        if (!hasLocationPermission()) {
            reportError("Permissão de localização necessária para iniciar a caminhada.")
            stopSelf()
            return START_NOT_STICKY
        }

        // A sticky restart has a null intent. An intentionally paused walk must remain paused
        // after process/service recreation; only an active session is eligible for automatic GPS resume.
        if (intent == null && walkingState?.isPaused == true) {
            stopLocationSource()
            stopSelf()
            return START_NOT_STICKY
        }

        promoteToForeground()
        Log.i(TAG, "onStartCommand action=" + intent?.action + ", restoredState=" + (walkingState?.isPaused ?: "null"))
        when (intent?.action) {
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stopWalkingSession()
            ACTION_CANCEL_START -> cancelPendingStartInternal()
            else -> beginTracking()
        }
        return START_STICKY
    }

    private fun beginTracking() {
        val app = container ?: return
        // Rebuild the runtime coordinator before starting a GPS source. A Service instance can be
        // rebound after Activity/process recreation with a restored read-model state but no in-memory
        // coordinator; the first simulated/real fix must still enter the shared runtime pipeline.
        if (walkingState != null) {
            app.resumePersistedWalk()?.walking?.let { restored ->
                walkingState = restored
            }
        }
        if (walkingState?.isPaused == true) {
            Log.i(TAG, "beginTracking: resuming persisted paused state")
            updateWalkingState(app.runtime.resumePaused(Instant.now()))
        }
        if (walkingState == null) {
            if (app.restorePreparedWalk() == null) {
                reportError("Não existe um plano de caminhada preparado.")
                stopSelf()
                return
            }
            pendingStart = true
        } else {
            pendingStart = false
        }
        pendingStartDistanceMeters = null
        startGuidanceIssued = false
        startLocationSource()
        notifyState()
    }

    private fun startLocationSource() {
        if (locationSource != null) return
        val app = container ?: return
        val route = app.publishedRoute()
        if (AndroidRouteCatalog.isTestRoute(route.id)) {
            val resumedKm = walkingState?.routePosition?.routeKm
            val startIndex = resumedKm?.let { GpxSimulationStartIndex.nextPointIndexAfterKm(route, it) }
                ?: GpxSimulationStartIndex.nearestPointIndex(
                    route,
                    app.restorePreparedWalk()?.walk?.plannedStartKm ?: 0.0
                )
            val source = GpxSimulationLocationSource(
                points = route.geometry.points,
                onPosition = ::handlePosition,
                onAvailabilityChanged = { available ->
                    if (!available && walkingState != null) {
                        updateWalkingState(app.runtime.markNoSignal(Instant.now()))
                    }
                },
                initialIndex = startIndex
            )
            // Publish the QA source before start() so commands queued during the synchronous
            // first-fix callback cannot race the assignment.
            simulationSource = source
            locationSource = source
            source.start()
        } else {
            val source = AndroidLocationSource(
                context = this,
                onPosition = ::handlePosition,
                onAvailabilityChanged = { available ->
                    if (!available && walkingState != null) {
                        updateWalkingState(app.runtime.markNoSignal(Instant.now()))
                    }
                },
                onLastKnownPosition = ::handleLastKnownPositionForGuidance
            )
            locationSource = source
            source.start()
        }
    }

    /**
     * A navigation hand-off must not wait for a brand-new GPS fix. When a device already
     * has a last-known position, use it only to decide whether start guidance is needed;
     * never promote that stale point into the walking state. A fresh GPS observation is
     * still required before the walk becomes ACTIVE.
     */
    private fun handleLastKnownPositionForGuidance(position: RawGpsPosition) {
        val app = container ?: return
        if (!pendingStart || walkingState != null || startGuidanceIssued) return
        val route = app.publishedRoute()
        if (AndroidRouteCatalog.isTestRoute(route.id)) return
        val routePosition = RouteLocationEngine.locate(route, position)
        val policy = com.caminhos2027.v1.core.route.GpsTrackingPolicy()
        val prepared = app.restorePreparedWalk()?.walk ?: return
        val target = WalkingGuidanceTargetPolicy.pointAtRouteKm(route, prepared.plannedStartKm ?: 0.0)
        val distanceToStartMeters = distanceMeters(position.latitude, position.longitude, target.latitude, target.longitude)
        if (distanceToStartMeters <= policy.startArrivalToleranceMeters) return
        startGuidanceIssued = true
        listeners.toList().forEach {
            it.onStartGuidanceNeeded(
                target,
                "Ir para o início da caminhada · " + route.officialName
            )
        }
    }

    private fun handlePosition(position: RawGpsPosition) {
        val app = container ?: return
        if (pendingStart && walkingState == null) {
            val route = app.publishedRoute()
            val routePosition = RouteLocationEngine.locate(route, position)
            val prepared = app.restorePreparedWalk()?.walk
            val targetKm = prepared?.plannedStartKm ?: 0.0
            val target = WalkingGuidanceTargetPolicy.pointAtRouteKm(route, targetKm)
            val policy = com.caminhos2027.v1.core.route.GpsTrackingPolicy()
            val distanceToStartMeters = distanceMeters(position.latitude, position.longitude, target.latitude, target.longitude)
            pendingStartDistanceMeters = distanceToStartMeters.takeIf { it.isFinite() }
            val isOnRoute = routePosition.distanceToRouteMeters < policy.possibleDeviationMeters
            val isAtPlannedStart = distanceToStartMeters <= policy.startArrivalToleranceMeters
            if (!isOnRoute || !isAtPlannedStart) {
                if (!startGuidanceIssued && !AndroidRouteCatalog.isTestRoute(route.id)) {
                    val guidance = WalkingGuidanceTargetPolicy.forPendingStart(
                        currentDistanceToRouteMeters = if (isAtPlannedStart) routePosition.distanceToRouteMeters else policy.possibleDeviationMeters,
                        possibleDeviationMeters = policy.possibleDeviationMeters,
                        plannedStart = target,
                        routeName = route.officialName
                    )
                    if (guidance != null) {
                        startGuidanceIssued = true
                        listeners.toList().forEach { it.onStartGuidanceNeeded(guidance.point, guidance.label) }
                    }
                }
                notifyState()
                return
            }
            try {
                val started = app.preparationController.startSaved(
                    catalog = app.publishedApoiCatalog(),
                    position = routePosition,
                    now = position.capturedAt
                ).walking ?: return
                app.attachWalk(started.walk)
                app.store.setWalking(started)
                walkingState = started
                pendingStart = false
                pendingStartDistanceMeters = null
                startGuidanceIssued = false
                notifyState()
            } catch (error: IllegalArgumentException) {
                Log.e(TAG, "Failed to start prepared walk from first GPS fix", error)
                reportError(error.message ?: "Não foi possível iniciar a caminhada.")
                notifyState()
            }
            return
        }
        if (walkingState != null) {
            val route = app.publishedRoute()
            val currentRoutePosition = RouteLocationEngine.locate(route, position)
            val possibleDeviationMeters = com.caminhos2027.v1.core.route.GpsTrackingPolicy().possibleDeviationMeters
            val lastKnownRoutePosition = walkingState?.routePosition
            val lastKnownOnRoutePoint = lastKnownRoutePosition?.projectedPoint
                ?: lastKnownRoutePosition?.let {
                    WalkingGuidanceTargetPolicy.pointAtRouteKm(route, it.routeKm)
                }
            val guidance = WalkingGuidanceTargetPolicy.forActiveWalk(
                currentDistanceToRouteMeters = currentRoutePosition.distanceToRouteMeters,
                possibleDeviationMeters = possibleDeviationMeters,
                lastKnownOnRoute = lastKnownOnRoutePoint
            )
            if (guidance != null && !startGuidanceIssued) {
                startGuidanceIssued = true
                if (!AndroidRouteCatalog.isTestRoute(app.publishedRoute().id)) {
                    listeners.toList().forEach { it.onStartGuidanceNeeded(guidance.point, guidance.label) }
                }
            } else if (guidance == null) {
                startGuidanceIssued = false
            }
            try {
                val updated = app.runtime.accept(position)
                Log.i(TAG, "GPS position accepted: routeKm=" + (updated.routePosition?.routeKm ?: "null") + ", distance=" + (updated.routePosition?.distanceToRouteMeters ?: "null") + ", state=" + updated.gpsState)
                updateWalkingState(updated)
            } catch (error: IllegalArgumentException) {
                reportError(error.message ?: "Posição GPS rejeitada.")
            }
        }
    }

    private fun updateWalkingState(state: WalkingState) {
        val previous = walkingState
        walkingState = state
        container?.store?.setWalking(state)
        notifyWalkingEvents(previous, state)
        notifyState()
    }

    private fun notifyWalkingEvents(previous: WalkingState?, current: WalkingState) {
        if (previous == null) return
        when {
            previous.gpsState != GpsState.NO_SIGNAL && current.gpsState == GpsState.NO_SIGNAL ->
                eventNotifier.notify("Sinal GPS perdido", "A última posição válida foi mantida. A aplicação não inventa movimento.")
            previous.gpsState == GpsState.NO_SIGNAL && current.gpsState == GpsState.ON_ROUTE ->
                eventNotifier.notify("Sinal GPS recuperado", "A posição voltou a estar no percurso.")
            previous.gpsState != GpsState.POSSIBLE_DEVIATION && current.gpsState == GpsState.POSSIBLE_DEVIATION ->
                eventNotifier.notify("Possível desvio", "Verifique a posição no mapa antes de continuar.")
            previous.gpsState != GpsState.PROBABLE_DEVIATION && current.gpsState == GpsState.PROBABLE_DEVIATION ->
                eventNotifier.notify("Provável desvio", "A posição atual está afastada do traçado oficial.")
            !previous.isPaused && current.isPaused ->
                eventNotifier.notify("Caminhada pausada", "A localização foi mantida e a caminhada pode ser retomada.")
            previous.isPaused && !current.isPaused ->
                eventNotifier.notify("Caminhada retomada", "O acompanhamento GPS voltou a estar ativo.")
            previous.nextApoi?.id != current.nextApoi?.id && current.nextApoi != null ->
                eventNotifier.notify(
                    "Próximo APOI",
                    current.nextApoi.name + " · " + (current.nextApoiDistanceKm?.let(::formatNotificationDistance) ?: "distância indisponível")
                )
        }
    }

    private fun pause() {
        val app = container ?: return
        if (walkingState == null) return
        updateWalkingState(app.runtime.pause(Instant.now()))
        stopLocationSource()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopWalkingSession() {
        val state = walkingState ?: run {
            cancelPendingStartInternal()
            return
        }
        val app = container ?: return
        val position = state.routePosition ?: app.runtime.lastKnownPosition()
        if (position == null) {
            reportError("Não existe uma posição válida para terminar a caminhada.")
            return
        }
        app.runtime.stop(position, Instant.now())
        app.clearSession()
        walkingState = null
        pendingStart = false
        pendingStartDistanceMeters = null
        startGuidanceIssued = false
        stopLocationSource()
        notifyState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun cancelPendingStartInternal() {
        pendingStart = false
        pendingStartDistanceMeters = null
        stopLocationSource()
        notifyState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopLocationSource() {
        locationSource?.stop()
        locationSource = null
        simulationSource = null
    }

    private fun promoteToForeground() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_location)
            .setContentTitle("Caminhada em curso")
            .setContentText("A localização está a ser acompanhada.")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Caminhada",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Acompanhamento GPS durante a caminhada"
                setShowBadge(false)
            }
        )
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusM = 6_371_008.8
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val dLat = p2 - p1
        val dLon = Math.toRadians(lon2 - lon1)
        val sinLat = kotlin.math.sin(dLat / 2.0)
        val sinLon = kotlin.math.sin(dLon / 2.0)
        val h = (sinLat * sinLat + kotlin.math.cos(p1) * kotlin.math.cos(p2) * sinLon * sinLon).coerceIn(0.0, 1.0)
        return 2.0 * earthRadiusM * kotlin.math.atan2(kotlin.math.sqrt(h), kotlin.math.sqrt(1.0 - h))
    }

    private fun notifyState() {
        listeners.toList().forEach { it.onTrackingStateChanged(walkingState, pendingStart, pendingStartDistanceMeters) }
    }

    private fun formatNotificationDistance(value: Double): String = if (value < 1.0) "${(value * 1000.0).toInt()} m" else String.format(java.util.Locale("pt", "PT"), "%.1f km", value)

    /**
     * QA controls can be invoked immediately after Activity/service binding, before the test
     * route source has finished initialization. Do not silently drop a command in that race.
     */
    private fun postWhenWalkingStateReady(
        action: () -> Unit,
        attemptsRemaining: Int = 20
    ) {
        mainHandler.post {
            if (container != null && walkingState != null) {
                action()
            } else if (attemptsRemaining > 0) {
                mainHandler.postDelayed(
                    { postWhenWalkingStateReady(action, attemptsRemaining - 1) },
                    100L
                )
            }
        }
    }

    private fun postWhenContainerReady(
        action: () -> Unit,
        attemptsRemaining: Int = 20
    ) {
        mainHandler.post {
            if (container != null) {
                action()
            } else if (attemptsRemaining > 0) {
                mainHandler.postDelayed(
                    { postWhenContainerReady(action, attemptsRemaining - 1) },
                    100L
                )
            }
        }
    }

    private fun postWhenSimulationReady(
        action: (GpxSimulationLocationSource) -> Unit,
        attemptsRemaining: Int = 20
    ) {
        mainHandler.post {
            val source = simulationSource
            if (source != null) {
                action(source)
            } else if (attemptsRemaining > 0) {
                mainHandler.postDelayed(
                    { postWhenSimulationReady(action, attemptsRemaining - 1) },
                    100L
                )
            }
        }
    }

    private fun reportError(message: String) {
        listeners.toList().forEach { it.onTrackingError(message) }
    }

    override fun onDestroy() {
        stopLocationSource()
        listeners.clear()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.caminhos2027.v1.walking.START"
        const val ACTION_RESUME = "com.caminhos2027.v1.walking.RESUME"
        const val ACTION_PAUSE = "com.caminhos2027.v1.walking.PAUSE"
        const val ACTION_STOP = "com.caminhos2027.v1.walking.STOP"
        const val ACTION_CANCEL_START = "com.caminhos2027.v1.walking.CANCEL_START"
        const val EXTRA_ROUTE_ID = "route_id"
        private const val CHANNEL_ID = "walking_tracking"
        private const val NOTIFICATION_ID = 2027
        private const val TAG = "CaminhosWalking"
    }
}
