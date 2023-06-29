package com.rokoblak.chatbackup

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationManagerCompat
import com.rokoblak.chatbackup.ui.notif.model.NotificationType
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChatBackupApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initNotificationChannels(this)
    }

    private fun initNotificationChannels(context: Context) {
        val notifManager = NotificationManagerCompat.from(context)
        val notificationChannels = NotificationType.values().toList().map {
            NotificationChannel(
                it.channelId,
                it.channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.TRANSPARENT
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        }
        notifManager.createNotificationChannels(notificationChannels)
    }
}