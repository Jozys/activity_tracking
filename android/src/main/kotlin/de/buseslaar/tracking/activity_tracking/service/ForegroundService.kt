package de.buseslaar.tracking.activity_tracking.service

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.notification.NotificationsHelper

open class ForegroundService() : Service() {


    var notification: Notification? = null;
    private var TAG = "FOREGROUND_SERVICE"
    open var notificationTitle = ActivityType.UNKNOWN.type;
    open var notificationText = "UNKNOWN";

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var notification =
            NotificationsHelper.buildNotification(
                this, notificationTitle, notificationText,
            );
        this.notification = notification;
        startForeground(1, notification)

        if (intent?.action == "ACTION_STOP_SERVICE") {
            stopSelf()

        }
        return START_STICKY;
    }


    open fun updateNotification(context: Context, title: String, text: String) {
        notification =
            NotificationsHelper.buildNotification(context, title, text);
        var mNotificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
        mNotificationManager.notify(1, notification);

    }


    open fun stopService(context: Context, sensorStop: () -> Unit) {
        val serviceIntent = Intent(context, this::class.java)

        serviceIntent.action = "ACTION_STOP_SERVICE"
        sensorStop()
        var intent = PendingIntent.getService(
            context,
            0,
            serviceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        intent.send()

        super.stopSelf();
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }


    open fun startService(context: Context, sensorStart: () -> Unit) {
        sensorStart()
        val serviceIntent = Intent(context, this::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

    }


}