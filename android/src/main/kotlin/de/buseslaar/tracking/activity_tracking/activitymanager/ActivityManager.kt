package de.buseslaar.tracking.activity_tracking.activitymanager


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import de.buseslaar.tracking.activity_tracking.activity.BikingActivity
import de.buseslaar.tracking.activity_tracking.activity.RunningActivity
import de.buseslaar.tracking.activity_tracking.activity.WalkingActivity
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.model.Event
import de.buseslaar.tracking.activity_tracking.notification.NotificationsHelper
import de.buseslaar.tracking.activity_tracking.notification.receiver.ActivityBroadcastReceiver
import de.buseslaar.tracking.activity_tracking.service.BikingForegroundService
import de.buseslaar.tracking.activity_tracking.service.ForegroundService
import de.buseslaar.tracking.activity_tracking.service.RunningForegroundService
import de.buseslaar.tracking.activity_tracking.service.WalkingForegroundService
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject
import kotlin.math.roundToInt

private const val TAG = "ACTIVITY_MANAGER"

class ActivityManager {

    var currentActivity: Activity? = null
    var isPaused: Boolean = false
    var eventSink: EventChannel.EventSink? = null
    var context: Context? = null;
    var foregroundService: ForegroundService? = null;

    constructor(newContext: Context) {
        this.context = newContext;
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startActivity(type: String): String {
        val activityType = ActivityType.valueOf(type);
        if (context != null) {
            when (activityType) {
                ActivityType.WALKING -> {
                    this.foregroundService = WalkingForegroundService(context!!)
                    this.currentActivity = WalkingActivity(
                        onLocationChanged = { onLocationChanged(it) },
                        onStepChanged = { onStepChanged(it) })
                }

                ActivityType.BIKING -> {
                    this.foregroundService = BikingForegroundService(context!!)
                    this.currentActivity =
                        BikingActivity(onLocationChanged = { onLocationChanged(it) })
                }

                ActivityType.RUNNING -> {
                    this.foregroundService = RunningForegroundService(context!!)
                    this.currentActivity = RunningActivity(
                        onLocationChanged = { onLocationChanged(it) },
                        onStepChanged = { onStepChanged(it) })
                }

                else -> {
                    return "Unknown Activity Type"
                }

            }
            this.foregroundService?.startService(context!!, {
                this.currentActivity?.startSensors(context!!)
            });
        }
        return currentActivity?.parseToJSON().toString()
    }

    fun togglePauseActivity(): Boolean {
        Log.d(TAG, "Current Activity: " + currentActivity?.type)

        if (currentActivity == null) return false

        if (isPaused) {
            this.currentActivity?.startSensors(context!!)
            isPaused = false
        } else {
            this.currentActivity?.stopSensors(context!!)
            isPaused = true
        }
        this.foregroundService?.updateNotification(
            context!!,
            currentActivity?.type.toString(),
            NotificationsHelper.generateActivityNotificationDescription(currentActivity),
            createTrackingActions(context!!)
        );

        return true;
    }

    fun stopCurrentActivity(): Activity? {
        Log.d(TAG, "Current Activity: " + currentActivity?.type)
        if (currentActivity == null) return null
        currentActivity?.endDateTime = System.currentTimeMillis()

        this.foregroundService?.stopService(context!!, {
            this.currentActivity!!.stopSensors(context!!);
        });
        return currentActivity
    }

    fun onStepChanged(addedSteps: Int) {
        currentActivity?.steps = currentActivity?.steps?.plus(addedSteps)!!
        Log.d(TAG, "Steps: " + currentActivity?.steps)
        eventSink?.success(constructJsonString<Int>(Event.STEP, addedSteps));
        this.foregroundService?.updateNotification(
            context!!,
            currentActivity?.type.toString(),
            NotificationsHelper.generateActivityNotificationDescription(currentActivity),
            createTrackingActions(context!!)
        );
    }

    fun onLocationChanged(locations: List<Location>) {
        Log.d(TAG, "Location Changed with ${currentActivity?.locations?.size} locations")
        Log.d(TAG, "Distance ${currentActivity?.distance}")
        for (location in locations) {
            Log.d(TAG, location.longitude.toString())
            if (currentActivity != null && currentActivity!!.locations.isNotEmpty()) {
                val lastLocation =
                    currentActivity?.locations?.entries?.maxByOrNull { it.key }?.value;
                Log.d(
                    TAG,
                    "Last Location: ${lastLocation?.latitude}, ${lastLocation?.longitude}, ${
                        lastLocation?.distanceTo(location)
                    }"
                )
                if (lastLocation == null) {
                    continue;
                }
                if (lastLocation.latitude == location.latitude && lastLocation.longitude == location.longitude) {
                    continue;
                }
                currentActivity?.distance =
                    (currentActivity?.distance?.plus(lastLocation.distanceTo(location))!!);
                currentActivity?.distance =
                    (currentActivity?.distance?.times(100.0))?.roundToInt()
                        ?.div(100.0)!!
            }
            currentActivity?.addLocation(
                location.time,
                de.buseslaar.tracking.activity_tracking.model.Location(
                    location.latitude,
                    location.longitude,
                    location.altitude,
                    location.speed
                )
            )
            this.foregroundService?.updateNotification(
                context!!,
                currentActivity?.type.toString(),
                NotificationsHelper.generateActivityNotificationDescription(currentActivity),
                createTrackingActions(context!!)
            );
            var lastLocationEntry =
                currentActivity?.locations?.entries?.maxByOrNull { it.key };
            if (lastLocationEntry != null) {
                eventSink?.success(
                    constructJsonString<MutableMap.MutableEntry<Long, de.buseslaar.tracking.activity_tracking.model.Location>>(
                        Event.LOCATION,
                        lastLocationEntry
                    )
                );
            }

            eventSink?.success(
                constructJsonString<Double?>(
                    Event.DISTANCE,
                    currentActivity?.distance
                )
            )
        }
    }



    fun <T> constructJsonString(key: Event, data: T): String {
        var json = JSONObject();
        json.put("type", key.type);
        when (key) {
            Event.STEP -> {
                json.put("data", data);
            }

            Event.LOCATION -> {
                var rawLocationData =
                    data as MutableMap.MutableEntry<Long, de.buseslaar.tracking.activity_tracking.model.Location>;
                val locationData = JSONObject();
                locationData.put("latitude", rawLocationData.value.latitude);
                locationData.put("longitude", rawLocationData.value.longitude);
                locationData.put("altitude", rawLocationData.value.altitude);
                locationData.put(
                    "speed",
                    rawLocationData.value.speed.times(10.0).roundToInt().div(10.0).toDouble()
                );

                locationData.put("pace", rawLocationData.value.pace.toDouble());
                val locationTime = JSONObject();
                locationTime.put(rawLocationData.key.toString(), locationData);
                json.put("data", locationTime);
            }

            Event.DISTANCE -> {
                json.put("data", data);
            }

            Event.STOP, Event.PAUSE, Event.RESUME -> {
                json.put("data", currentActivity?.parseToJSON());
            }

            else -> {
                var error = JSONObject();
                error.put("type", "error");
                error.put("error", "Invalid data type");
                return error.toString();
            }
        }
        return json.toString();
    }


    internal fun createTrackingActions(context: Context): List<NotificationCompat.Action> {
        val actions = mutableListOf<NotificationCompat.Action>()
        ActivityBroadcastReceiver.setActivityManager(this)

        // Pause Action
        val pauseIntent = Intent(context, ActivityBroadcastReceiver::class.java).apply {
            action = "${context.applicationContext.packageName}.PAUSE"
        }
        val pausePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIcon = if (isPaused) {
            android.R.drawable.ic_media_play
        } else {
            android.R.drawable.ic_media_pause
        }

        val pauseText = if (isPaused) {
            "Resume"
        } else {
            "Pause"
        }

        val pauseAction = NotificationCompat.Action(
            pauseIcon,
            pauseText,
            pausePendingIntent
        )
        actions.add(pauseAction)

        // Stop Action
        val stopIntent = Intent(context, ActivityBroadcastReceiver::class.java).apply {
            action = "${context.applicationContext.packageName}.STOP"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_media_ff,
            "Stop",
            stopPendingIntent
        )
        actions.add(stopAction)

        return actions
    }

}

