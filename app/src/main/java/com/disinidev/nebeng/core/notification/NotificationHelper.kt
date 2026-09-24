package com.disinidev.nebeng.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.disinidev.nebeng.MainActivity
import com.disinidev.nebeng.R

object NotificationHelper {

    const val CHANNEL_TRIP = "channel_trip_updates"
    const val CHANNEL_CHAT = "channel_chat_messages"
    const val CHANNEL_SYSTEM = "channel_system"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val tripChannel = NotificationChannel(
                CHANNEL_TRIP,
                "Update Perjalanan & Tebengan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi status penjemputan, live tracking, dan tebengan selesai"
                enableVibration(true)
            }

            val chatChannel = NotificationChannel(
                CHANNEL_CHAT,
                "Pesan & Chat Driver",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pesan obrolan masuk dari pengemudi atau penumpang"
                enableVibration(true)
            }

            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "Informasi & Sistem Nebeng",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengumuman dan informasi akun Nebeng"
            }

            notificationManager.createNotificationChannels(listOf(tripChannel, chatChannel, systemChannel))
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String = CHANNEL_TRIP,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Handled when Android 13 POST_NOTIFICATIONS is not yet granted
        }
    }
}
