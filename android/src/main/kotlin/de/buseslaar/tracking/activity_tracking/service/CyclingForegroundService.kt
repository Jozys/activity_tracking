package de.buseslaar.tracking.activity_tracking.service

import android.content.Context
import android.util.Log
import de.buseslaar.tracking.activity_tracking.model.ActivityType

class CyclingForegroundService() : ForegroundService() {

    override var notificationTitle: String = ActivityType.CYCLING.type;
    override var notificationText: String = "Distance: 0, Speed: 0";
    private var TAG = "CYCLING_FOREGROUND_SERVICE";
    private var context: Context? = null

    constructor(context: Context) : this() {
        this.context = context
        this.createNotificationChannel(context)
    }

    override fun startService(context: Context, sensorStart: () -> Unit) {
        Log.d(TAG, "Starting Cycling foreground service")
        super.startService(context, sensorStart)
    }
}
