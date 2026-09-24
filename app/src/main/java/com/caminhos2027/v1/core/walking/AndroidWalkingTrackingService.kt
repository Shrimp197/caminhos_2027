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
    }

    inner class TrackingBinder : Binder() {
        fun register(listener: Listener) {
            listeners += listener
            listener.onTrackingStateChanged(walkingState, pendingStart, pendingStartDistanceMeters)
        }
        fun unregister(listener: Listener) { listeners -= listener }
        fun startTracking() = beginTracking()
        fun pauseWalking() = pause()
        fun resumeWalking() = beginTracking()
        fun stopWalking() = stopWalkingSession()
        fun cancelPendingStart() = cancelPendingStartInternal()
        fun qaAdvance() { postWhenSimulationReady(action = { source -> source.advance() }) }
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
        fun qaSimulateDeviation() { postWhenSimulationReady(action = { source -> source.simulateDeviation() }) }
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
        if (walkingState?.isPaused == true) {
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
                }
            )
            locationSource = source
            source.start()
        }
    }

    private fun handlePosition(position: RawGpsPosition) {
        val app = container ?: return
        if (pendingStart && walkingState == null) {
            val routePosition = RouteLocationEngine.locate(app.publishedRoute(), position)
            pendingStartDistanceMeters = routePosition.distanceToRouteMeters.takeIf { it.isFinite() }
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
                notifyState()
            } catch (error: IllegalArgumentException) {
                Log.e(TAG, "Failed to start prepared walk from first GPS fix", error)
                reportError(error.message ?: "Não foi possível iniciar a caminhada.")
                notifyState()
            }
            return
        }
        if (walkingState != null) {
            try {
                updateWalkingState(app.runtime.accept(position))
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
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun notifyState() {
        listeners.toList().forEach { it.onTrackingStateChanged(walkingState, pendingStart, pendingStartDistanceMeters) }
    }

    private fun formatNotificationDistance(value: Double): String = if (value < 1.0) "${(value * 1000.0).toInt()} m" else String.format(java.util.Locale("pt", "PT"), "%.1f km", value)

    /**
     * QA controls can be invoked immediately after Activity/service binding, before the test
     * route source has finished initialization. Do not silently drop a command in that race.
     */
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
