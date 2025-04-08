package de.buseslaar.tracking.activity_tracking.service

import android.content.Context
import android.util.Log
import de.buseslaar.tracking.activity_tracking.model.ActivityType

class RunningForegroundService : WalkingForegroundService {

    override var notificationTitle: String = ActivityType.RUNNING.type
    override var notificationText: String = "Loading..."
    private var TAG = "RUNNING_FOREGROUND_SERVICE"

    constructor() : super() {
        // Default constructor
    }

    constructor(context: Context) : super(context) {
        this.context = context;
        this.createNotificationChannel(context)
    }

    override fun startService(context: Context, sensorStart: () -> Unit) {
        Log.d(TAG, "Starting Running Foreground Service")
        super.startService(context, sensorStart)
    }
}