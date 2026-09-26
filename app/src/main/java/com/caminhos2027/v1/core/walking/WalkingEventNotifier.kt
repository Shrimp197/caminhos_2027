package com.caminhos2027.v1.core.walking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.caminhos2027.R

class WalkingEventNotifier(
    context: Context
) {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(NotificationManager::class.java)
    private var nextId = 4000

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Eventos da caminhada",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "GPS, apoios, pausas e orientações relevantes durante a caminhada"
                }
            )
        }
    }

    fun notify(title: String, text: String) {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                appContext,
                nextId,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_location)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        manager.notify(nextId++, notification)
    }

    companion object {
        private const val CHANNEL_ID = "walking_events"
    }
}
