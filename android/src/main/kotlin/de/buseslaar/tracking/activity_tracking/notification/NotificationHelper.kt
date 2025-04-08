package de.buseslaar.tracking.activity_tracking.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import de.buseslaar.tracking.activity_tracking.R
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.model.ActivityType

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
            .setOngoing(true)
            .setSilent(true)

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


    fun generateActivityNotificationDescription(
        currentActivity: Activity?,
        context: Context,
        duration: Boolean = false
    ): String {
        var notification = "";
        if (currentActivity == null) return notification;
        when (currentActivity?.type) {
            ActivityType.WALKING, ActivityType.RUNNING -> {
                notification =
                    "${
                        getLocalizedString(
                            R.string.steps,
                            context
                        )
                    }: " + currentActivity?.steps.toString() + " "
            }

            else -> {}
        }
        if (currentActivity?.locations?.isEmpty() == true) return ""
        notification = notification +
                "${
                    getLocalizedString(
                        R.string.speed,
                        context
                    )
                }: " + ((currentActivity?.locations?.entries?.maxByOrNull { it.key }?.value?.speed?.toString()) + " km/h ")

        if (currentActivity.distance == null) return notification;
        notification =
            notification + " ${
                getLocalizedString(
                    R.string.distance,
                    context
                )
            }: " + (currentActivity?.distance).toString() + " km";

        if (duration) {
            notification = notification + " ${
                getLocalizedString(
                    R.string.duration,
                    context
                )
            }: " + currentActivity.endDateTime?.minus(currentActivity.startDateTime!!)
                ?.let { formateDateToHours(it) } + "";
        }
        return notification;
    }

    fun getLocalizedString(resourceId: Int, context: Context): String {
        return context.getString(resourceId).toString();
    }

    @SuppressLint("DefaultLocale")
    fun formateDateToHours(millis: Long): String {
        // Millis to seconds
        val fullSeconds = (millis / 1000)

        val hours = fullSeconds / 3600
        val minutes = (fullSeconds % 3600) / 60
        val seconds = fullSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}