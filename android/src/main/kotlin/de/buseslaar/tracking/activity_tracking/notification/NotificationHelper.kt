package de.buseslaar.tracking.activity_tracking.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

    private fun getLaunchIntent(context: Context): PendingIntent? {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
        return if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else null
    }

    fun buildNotification(
        context: Context,
        title: String,
        description: String,
        actions: List<NotificationCompat.Action> = emptyList()
    ): Notification {

        var notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOnlyAlertOnce(true)
        var launchIntent = getLaunchIntent(context)
        if (launchIntent != null) {
            notification.setContentIntent(launchIntent)
                .setAutoCancel(false)
        }

        actions.forEach { action ->

            notification.addAction(action)
        }
        return notification.build();
    }

}