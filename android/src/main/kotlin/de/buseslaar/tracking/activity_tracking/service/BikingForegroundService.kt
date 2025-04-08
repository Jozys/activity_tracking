package de.buseslaar.tracking.activity_tracking.service

import android.content.Context
import android.util.Log
import de.buseslaar.tracking.activity_tracking.model.ActivityType

class BikingForegroundService() : ForegroundService() {

    override var notificationTitle: String = ActivityType.BIKING.type;
    override var notificationText: String = "Loading...";
    private var TAG = "BIKING_FOREGROUND_SERVICE";
    private var context: Context? = null

    constructor(context: Context) : this() {
        this.context = context
        this.createNotificationChannel(context)
    }

    override fun startService(context: Context, sensorStart: () -> Unit) {
        Log.d(TAG, "Starting Biking foreground service")
        super.startService(context, sensorStart)
    }
}
