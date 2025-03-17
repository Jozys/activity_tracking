package de.buseslaar.tracking.activity_tracking.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat

object NotificationsHelper {

    private const val NOTIFICATION_CHANNEL_ID = "ActivityTracking"

    fun createNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        title: String,
        description: String,
        action: NotificationCompat.Action? = null
    ): Notification {
        var notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(android.R.drawable.ic_media_play)
        if (action != null) {
            notification.addAction(action)
        }
        return notification.build();
    }
}