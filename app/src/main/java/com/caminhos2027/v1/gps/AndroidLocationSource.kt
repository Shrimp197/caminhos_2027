package com.caminhos2027.v1.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.caminhos2027.v1.core.model.RawGpsPosition
import java.time.Instant

/** Thin Android adapter. It reports raw device positions; route projection stays in the domain layer. */
class AndroidLocationSource(
    context: Context,
    private val onPosition: (RawGpsPosition) -> Unit,
    private val onAvailabilityChanged: (Boolean) -> Unit = {}
) : LocationSource {
    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var started = false
    private var updatesRegistered = false
    private var lastAvailability: Boolean? = null

    private val availabilityMonitor = object : Runnable {
        override fun run() {
            if (!started) return
            refreshAvailability()
            mainHandler.postDelayed(this, AVAILABILITY_CHECK_INTERVAL_MS)
        }
    }

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val position = RawGpsPosition(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
                capturedAt = Instant.ofEpochMilli(location.time)
            )
            try {
                onPosition(position)
            } catch (error: IllegalArgumentException) {
                Log.w(TAG, "Location rejected by walking state: ${error.message}")
            }
        }

        override fun onProviderEnabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                refreshAvailability()
            }
        }

        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                refreshAvailability()
            }
        }

        @Deprecated("Required for LocationListener compatibility")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        if (started) return
        started = true
        refreshAvailability()
        mainHandler.post(availabilityMonitor)
    }

    @SuppressLint("MissingPermission")
    private fun refreshAvailability() {
        if (!started) return
        val available = hasLocationService() && hasGpsProvider()
        if (available) {
            if (!updatesRegistered) {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL_MS,
                        MIN_DISPLACEMENT_METERS,
                        listener,
                        Looper.getMainLooper()
                    )
                    updatesRegistered = true
                } catch (error: SecurityException) {
                    updatesRegistered = false
                    Log.w(TAG, "Unable to register GPS updates", error)
                }
            }
        } else if (updatesRegistered) {
            locationManager.removeUpdates(listener)
            updatesRegistered = false
        }

        if (lastAvailability != available) {
            lastAvailability = available
            onAvailabilityChanged(available)
        }
    }

    override fun stop() {
        if (!started) return
        started = false
        mainHandler.removeCallbacks(availabilityMonitor)
        if (updatesRegistered) {
            locationManager.removeUpdates(listener)
            updatesRegistered = false
        }
        lastAvailability = null
    }

    private fun hasGpsProvider(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun hasLocationService(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            hasGpsProvider()
        }

    private companion object {
        const val TAG = "AndroidLocationSource"
        const val UPDATE_INTERVAL_MS = 2000L
        const val MIN_DISPLACEMENT_METERS = 5f
        const val AVAILABILITY_CHECK_INTERVAL_MS = 3000L
    }
}
