package com.disinidev.nebeng

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre

@HiltAndroidApp
class NebengApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        com.disinidev.nebeng.core.notification.NotificationHelper.createNotificationChannels(this)
    }
}
