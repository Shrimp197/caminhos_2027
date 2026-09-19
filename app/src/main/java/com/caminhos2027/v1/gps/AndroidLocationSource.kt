package com.caminhos2027.v1.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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
                // A prepared walk may legitimately reject a first fix that is outside the
                // possible-deviation threshold. That is a domain/UI condition, not a reason
                // for the Android location callback to terminate the application.
                Log.w(TAG, "Location rejected by walking state: ${error.message}")
            }
        }

        override fun onProviderEnabled(provider: String) {
            onAvailabilityChanged(true)
        }

        override fun onProviderDisabled(provider: String) {
            onAvailabilityChanged(false)
        }

        @Deprecated("Required for LocationListener compatibility")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    @SuppressLint("MissingPermission")
    override fun start() {
        if (!hasProvider()) {
            onAvailabilityChanged(false)
            return
        }
        onAvailabilityChanged(true)
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L,
            5f,
            listener
        )
    }

    override fun stop() {
        locationManager.removeUpdates(listener)
    }

    private fun hasProvider(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private companion object {
        const val TAG = "AndroidLocationSource"
    }
}
