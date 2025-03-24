package de.buseslaar.tracking.activity_tracking.service

import android.content.Context
import android.util.Log
import de.buseslaar.tracking.activity_tracking.model.ActivityType

open class WalkingForegroundService() : ForegroundService() {

    override var notificationTitle: String = ActivityType.WALKING.type
    override var notificationText: String = "Steps: 0"
    private var TAG = "WALKING_FOREGROUND_SERVICE"
    internal var context: Context? = null;

    constructor(context: Context) : this() {
        this.context = context;
        this.createNotificationChannel(context)

    }

    override fun startService(context: Context, sensorStart: () -> Unit) {
        Log.d(TAG, "Starting Walking Foreground Service")
        super.startService(context, sensorStart)
    }
}