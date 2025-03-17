package de.buseslaar.tracking.activity_tracking.service

import android.content.Context
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.notification.NotificationsHelper

open class WalkingForegroundService() : ForegroundService() {

    override var notificationTitle: String
        get() = ActivityType.WALKING.type;
        set(value) {}
    override var notificationText: String
        get() = "Steps: 0"
        set(value) {}
    private var TAG = "WALKING_FOREGROUND_SERVICE"
    private var context: Context? = null;

    constructor(context: Context) : this() {
        this.context = context;
        try {
            NotificationsHelper.createNotificationChannel(context);
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    override fun updateNotification(context: Context, title: String, text: String) {
        super.updateNotification(context, title, text)
    }

    override fun startService(context: Context, sensorStart: () -> Unit) {
        super.startService(context, sensorStart)
    }
}