package de.buseslaar.tracking.activity_tracking.activitymanager


import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.activity.CyclingActivity
import de.buseslaar.tracking.activity_tracking.activity.RunningActivity
import de.buseslaar.tracking.activity_tracking.activity.WalkingActivity
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.notification.NotificationsHelper
import de.buseslaar.tracking.activity_tracking.service.CyclingForegroundService
import de.buseslaar.tracking.activity_tracking.service.ForegroundService
import de.buseslaar.tracking.activity_tracking.service.RunningForegroundService
import de.buseslaar.tracking.activity_tracking.service.WalkingForegroundService
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject
import kotlin.math.roundToInt

private const val TAG = "ACTIVITY_MANAGER"

class ActivityManager {

    private var currentActivity: Activity? = null
    var eventSink: EventChannel.EventSink? = null
    var context: Context? = null;
    var foregroundService: ForegroundService? = null;

    constructor(newContext: Context) {
        this.context = newContext;
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startActivity(type: String): String {
        var activityType = ActivityType.valueOf(type);
        if (context != null) {
            when (activityType) {
                ActivityType.WALKING -> {
                    this.foregroundService = WalkingForegroundService(context!!)
                    this.currentActivity = WalkingActivity(
                        onLocationChanged = { onLocationChanged(it) },
                        onStepChanged = { onStepChanged(it) })
                }

                ActivityType.CYCLING -> {
                    this.foregroundService = CyclingForegroundService(context!!)
                    this.currentActivity =
                        CyclingActivity(onLocationChanged = { onLocationChanged(it) })
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
        return type;
    }

    fun stopCurrentActivity(): Activity? {
        Log.d(TAG, "Current Activity: " + currentActivity?.type)//(currentActivity?.steps);
        if (currentActivity == null) return null
        currentActivity?.endDateTime = System.currentTimeMillis()

        this.foregroundService?.stopService(context!!, {
            this.currentActivity!!.stopSensors(context!!);
        });

        var notificationManager =
            context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
        notificationManager.notify(
            System.currentTimeMillis().toInt(), NotificationsHelper.buildNotification(
                context!!,
                "Finished: ${currentActivity?.type.toString()}",
                generateNotificationDescription()

            )
        );
        return currentActivity
    }

    fun onStepChanged(addedSteps: Int) {
        currentActivity?.steps = currentActivity?.steps?.plus(addedSteps)!!
        Log.d(TAG, "Steps: " + currentActivity?.steps)
        eventSink?.success(constructJsonString<Int>("step", addedSteps));
        this.foregroundService?.updateNotification(
            context!!,
            currentActivity?.type.toString(),
            generateNotificationDescription()
        );
    }

    fun onLocationChanged(locations: List<Location>) {
        for (location in locations) {
            Log.d(TAG, location.longitude.toString())
            if (currentActivity != null && currentActivity!!.locations.isNotEmpty()) {
                val lastLocation = currentActivity?.locations?.values?.last();
                if (lastLocation == null) {
                    continue;
                }
                if (lastLocation.latitude == location.latitude && lastLocation.longitude == location.longitude) {
                    continue;
                }
                currentActivity?.distance =
                    (currentActivity?.distance?.plus(lastLocation.distanceTo(location))!! * 1000.0).roundToInt() / 1000.0;
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
                generateNotificationDescription()
            );
            eventSink?.success(constructJsonString<Location>("location", location));
            eventSink?.success(
                constructJsonString<Double?>(
                    "distance",
                    currentActivity?.distance
                )
            )
        }
    }

    fun generateNotificationDescription(): String {
        var notification = "";
        if (currentActivity == null) return notification;
        when (currentActivity?.type) {
            ActivityType.WALKING, ActivityType.RUNNING -> {
                notification = "Steps: " + currentActivity?.steps.toString() + ";"
            }

            else -> {}
        }
        if (currentActivity?.locations?.isEmpty() == true) return ""
        notification = notification +
                "Speed: " + ((currentActivity?.locations?.values?.last()?.speed?.times(
            3.6F
        )?.times(10.0))?.roundToInt()
            ?.div(10.0)).toString() + " km/h;"

        if (currentActivity?.distance == null) return notification;
        notification =
            notification + " Distance: " + (currentActivity?.distance).toString() + " km";
        return notification;


    }

    private fun <T> constructJsonString(key: String, data: T): String {
        var json = JSONObject();
        json.put("type", key);
        when (key) {
            "step" -> {
                json.put("data", data);
            }

            "location" -> {
                var rawLocationData = data as Location;
                val locationData = JSONObject();
                locationData.put("latitude", rawLocationData.latitude);
                locationData.put("longitude", rawLocationData.longitude);
                locationData.put("altitude", rawLocationData.altitude);
                locationData.put("speed", rawLocationData.speed.toDouble());
                val locationTime = JSONObject();
                locationTime.put(rawLocationData.time.toString(), locationData);
                json.put("data", locationTime);
            }

            "distance" -> {
                json.put("data", data);
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

}

