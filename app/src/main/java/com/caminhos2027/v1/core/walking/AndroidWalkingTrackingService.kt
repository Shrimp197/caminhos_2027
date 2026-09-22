package com.caminhos2027.v1.core.walking

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.caminhos2027.v1.core.AndroidV1AppContainer
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.route.RouteLocationEngine
import com.caminhos2027.v1.gps.AndroidLocationSource
import java.time.Instant

/**
 * Owns real-device location tracking independently from the Activity lifecycle.
 *
 * The Activity starts this service while visible after location permission is granted.
 * The service then keeps the GPS pipeline alive when the Activity is stopped/recreated.
 */
class AndroidWalkingTrackingService : Service() {
    private var locationSource: AndroidLocationSource? = null
    private var appContainer: AndroidV1AppContainer? = null
    private var pendingStart = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_RESUME
        when (action) {
            ACTION_START -> {
                pendingStart = true
                val routeId = intent.getStringExtra(EXTRA_ROUTE_ID)
                    ?: AndroidRouteCatalog.preferredPersistedRouteId(this)
                    ?: AndroidRouteCatalog.CENTENARIO_ID
                ensureContainer(routeId)
                if (!beginForeground()) return START_NOT_STICKY
                startLocation()
            }

            ACTION_RESUME -> {
                val active = AndroidV1AppContainer(this).runtime.activeWalk()
                if (active == null) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                pendingStart = false
                ensureContainer(active.routeId)
                if (!beginForeground()) return START_NOT_STICKY
                val restored = appContainer!!.resumePersistedWalk()
                if (restored.walking?.isPaused == true) {
                    stopTrackingAndSelf()
                } else {
                    startLocation()
                    broadcastState()
                }
            }

            ACTION_PAUSE -> {
                pauseAndStop()
            }

            ACTION_STOP -> {
                stopWalkAndSelf()
            }

            ACTION_CANCEL -> {
                stopTrackingAndSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocation()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureContainer(routeId: String) {
        if (appContainer?.publishedRoute()?.id != routeId) {
            appContainer = AndroidV1AppContainer(this, routeId)
        }
    }

    private fun beginForeground(): Boolean {
        val hasLocationPermission =
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) {
            stopSelf()
            return false
        }

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Caminhos do Peregrino")
            .setContentText("A caminhada está a ser acompanhada por GPS.")
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return true
    }

    private fun startLocation() {
        if (locationSource != null) return
        val container = requireNotNull(appContainer)
        val source = AndroidLocationSource(
            context = this,
            onPosition = ::handlePosition,
            onAvailabilityChanged = { available ->
                if (!available) {
                    val active = container.runtime.activeWalk()
                    if (active != null) {
                        runCatching {
                            container.runtime.resume(Instant.now())
                            container.runtime.markNoSignal(Instant.now())
                            broadcastState()
                        }
                    }
                }
            }
        )
        locationSource = source
        source.start()
    }

    private fun handlePosition(position: RawGpsPosition) {
        val container = requireNotNull(appContainer)
        val active = container.runtime.activeWalk()
        if (active == null && pendingStart) {
            val routePosition = RouteLocationEngine.locate(container.publishedRoute(), position)
            val distance = routePosition.distanceToRouteMeters
            val started = runCatching {
                container.preparationController.startSaved(
                    catalog = container.publishedApoiCatalog(),
                    position = routePosition,
                    now = position.capturedAt
                )
            }.getOrNull()

            if (started?.walking != null) {
                pendingStart = false
                container.resumePersistedWalk(position.capturedAt)
                broadcastState(distance)
            } else {
                broadcastPendingStart(distance)
            }
            return
        }

        if (active != null) {
            runCatching {
                container.runtime.resume(position.capturedAt)
                container.runtime.accept(position)
                broadcastState()
            }
        }
    }

    private fun pauseAndStop() {
        val container = appContainer ?: return
        val active = container.runtime.activeWalk() ?: run {
            stopTrackingAndSelf()
            return
        }
        runCatching {
            container.runtime.resume(Instant.now())
            container.runtime.pause(Instant.now())
            broadcastState()
        }
        pendingStart = false
        stopTrackingAndSelf()
    }

    private fun stopWalkAndSelf() {
        val container = appContainer
        val active = container?.runtime?.activeWalk()
        if (container != null && active != null) {
            runCatching {
                container.runtime.resume(Instant.now())
                val position = container.runtime.lastKnownPosition()
                if (position != null) container.runtime.stop(position, Instant.now())
            }
        }
        pendingStart = false
        sendBroadcast(ACTION_STATE_CHANGED)
        stopTrackingAndSelf()
    }

    private fun broadcastState(pendingDistanceMeters: Double? = null) {
        val intent = Intent(ACTION_STATE_CHANGED).setPackage(packageName)
        appContainer?.runtime?.activeWalk()?.let { intent.putExtra(EXTRA_ROUTE_ID, it.routeId) }
        if (pendingDistanceMeters != null) {
            intent.putExtra(EXTRA_PENDING_DISTANCE, pendingDistanceMeters)
        }
        sendBroadcast(intent)
    }

    private fun broadcastPendingStart(distanceMeters: Double) {
        val intent = Intent(ACTION_STATE_CHANGED)
            .setPackage(packageName)
            .putExtra(EXTRA_PENDING_DISTANCE, distanceMeters)
        sendBroadcast(intent)
    }

    private fun stopLocation() {
        locationSource?.stop()
        locationSource = null
    }

    private fun stopTrackingAndSelf() {
        stopLocation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Acompanhamento da caminhada",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Indica quando o GPS da caminhada está ativo."
            }
        )
    }

    companion object {
        const val ACTION_START = "com.caminhos2027.v1.walking.START"
        const val ACTION_RESUME = "com.caminhos2027.v1.walking.RESUME"
        const val ACTION_PAUSE = "com.caminhos2027.v1.walking.PAUSE"
        const val ACTION_STOP = "com.caminhos2027.v1.walking.STOP"
        const val ACTION_CANCEL = "com.caminhos2027.v1.walking.CANCEL"
        const val ACTION_STATE_CHANGED = "com.caminhos2027.v1.walking.STATE_CHANGED"
        const val EXTRA_ROUTE_ID = "route_id"
        const val EXTRA_PENDING_DISTANCE = "pending_distance_meters"
        private const val CHANNEL_ID = "walking_tracking"
        private const val NOTIFICATION_ID = 2027
    }
}
