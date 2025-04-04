package de.buseslaar.tracking.activity_tracking.notification.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.activitymanager.ActivityManager
import de.buseslaar.tracking.activity_tracking.model.Event

class ActivityBroadcastReceiver() : BroadcastReceiver() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var activityManagerInstance: ActivityManager? = null

        fun setActivityManager(manager: ActivityManager) {
            activityManagerInstance = manager
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onReceive(context: Context?, intent: Intent?) {
        var packageName = ""
        if (context != null && context.applicationContext != null) {
            packageName = context.applicationContext.packageName
        } else {
            return;
        }
        when (intent?.action) {

            "$packageName.PAUSE" -> {
                if (activityManagerInstance != null && context.applicationContext != null) {
                    if (activityManagerInstance!!.isPaused) {
                        activityManagerInstance!!.isPaused = false
                        activityManagerInstance!!.currentActivity?.startSensors(context.applicationContext)
                    } else {
                        activityManagerInstance!!.isPaused = true
                        activityManagerInstance!!.currentActivity?.stopSensors(context.applicationContext)
                    }
                    activityManagerInstance!!.foregroundService?.updateNotification(
                        context,
                        activityManagerInstance!!.currentActivity?.type.toString(),
                        activityManagerInstance!!.generateNotificationDescription(),
                        activityManagerInstance!!.createTrackingActions(context)
                    )
                    activityManagerInstance!!.eventSink?.success(
                        activityManagerInstance?.constructJsonString(
                            if (activityManagerInstance!!.isPaused) {
                                Event.RESUME
                            } else {
                                Event.PAUSE
                            },
                            null
                        )
                    )
                }
            }

            "$packageName.STOP" -> {
                if (activityManagerInstance != null && context.applicationContext != null) {

                    activityManagerInstance!!.stopCurrentActivity()
                    val notificationManager =
                        context.applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()
                    activityManagerInstance!!.eventSink?.success(
                        activityManagerInstance?.constructJsonString(
                            Event.STOP,
                            null
                        )
                    )
                }

            }
        }
    }
}