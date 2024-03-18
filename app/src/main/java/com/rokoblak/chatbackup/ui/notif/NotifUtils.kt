package com.rokoblak.chatbackup.ui.notif

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import com.rokoblak.chatbackup.R
import com.rokoblak.chatbackup.feature.main.MainActivity
import com.rokoblak.chatbackup.ui.notif.model.NotificationType

object NotifUtils {

    const val RESULT_BUNDLE_KEY = "res-bundle-id"
    const val EXTRA_ADDRESS = "extra-address"
    const val EXTRA_NOTIF_ID = "extra-notif-id"

    private fun getNotifId(address: String, body: String) = "notif_$address$body".hashCode()

    fun showIncomingSMSNotif(
        context: Context,
        title: String,
        body: String,
        address: String
    ) {
        val notifId = getNotifId(address = address, body = body)

        val replyLabel = "Enter your reply"
        val remoteInput: RemoteInput = RemoteInput.Builder(RESULT_BUNDLE_KEY)
            .setLabel(replyLabel)
            .build()

        val resultIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = "SMS-received-$address"
            putExtras(
                bundleOf(
                    EXTRA_ADDRESS to address,
                    EXTRA_NOTIF_ID to notifId.toString()
                )
            )
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            0 or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            flags
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            flags
        )

        val icon: IconCompat = IconCompat.createWithResource(
            context,
            R.drawable.ic_launcher_foreground
        )

        val replyAction: NotificationCompat.Action =
            NotificationCompat.Action.Builder(icon, "Reply", resultPendingIntent)
                .addRemoteInput(remoteInput)
                .build()

        val newMessageNotification =
            NotificationCompat.Builder(context, NotificationType.Main.channelId)
                .setupStyle(context)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .addAction(replyAction).build()

        context.notifManager().notify(notifId, newMessageNotification)
    }

    fun updateReplySentNotif(context: Context, notifId: Int) {
        val updatedNotif =
            NotificationCompat.Builder(context, NotificationType.Main.channelId)
                .setupStyle(context)
                .setContentText("Reply sent")
                .build()
        val notifManager = context.notifManager()
        notifManager.cancel(notifId)
        notifManager.notify(notifId, updatedNotif)
    }

    private fun Context.notifManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun NotificationCompat.Builder.setupStyle(context: Context) =
        setColor(ContextCompat.getColor(context, R.color.black))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
}