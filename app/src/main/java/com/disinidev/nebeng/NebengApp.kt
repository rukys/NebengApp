package com.disinidev.nebeng

import android.app.Application
import com.disinidev.nebeng.core.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import timber.log.Timber

@HiltAndroidApp
class NebengApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        MapLibre.getInstance(this)
        NotificationHelper.createNotificationChannels(this)
    }
}

