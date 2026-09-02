package com.caminhos2027.v1.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.caminhos2027.v1.core.model.RawGpsPosition
import java.time.Instant

/** Thin Android adapter. It reports raw device positions; route projection stays in the domain layer. */
class AndroidLocationSource(
    context: Context,
    private val onPosition: (RawGpsPosition) -> Unit,
    private val onAvailabilityChanged: (Boolean) -> Unit = {}
) {
    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onPosition(
                RawGpsPosition(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
                    capturedAt = Instant.ofEpochMilli(location.time)
                )
            )
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
    fun start() {
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

    fun stop() {
        locationManager.removeUpdates(listener)
    }

    private fun hasProvider(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}
