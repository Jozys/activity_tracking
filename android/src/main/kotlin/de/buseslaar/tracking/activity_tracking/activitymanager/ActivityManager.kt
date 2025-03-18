package de.buseslaar.tracking.activity_tracking.activitymanager


import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.notification.NotificationsHelper
import de.buseslaar.tracking.activity_tracking.sensor.LocationSensor
import de.buseslaar.tracking.activity_tracking.sensor.StepSensor
import de.buseslaar.tracking.activity_tracking.service.WalkingForegroundService
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject
import kotlin.math.roundToInt

private const val TAG = "ACTIVITY_MANAGER"

class ActivityManager {

    private var currentActivity: Activity? = null
    private var stepSensor: StepSensor? = null
    private var locationSensor: LocationSensor? = null
    var eventSink: EventChannel.EventSink? = null
    var context: Context? = null;
    var foregroundService: WalkingForegroundService? = null;

    constructor(newContext: Context) {
        this.stepSensor = StepSensor(newContext, onStepChanged = {
            onStepChanged(it)
        })
        this.locationSensor = LocationSensor(newContext, onLocationUpdatedListener = {
            onLocationChanged(it)
        })
        this.context = newContext;

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startActivity(type: String): String {
        currentActivity = Activity(ActivityType.valueOf(type))
        if (context != null) {
            this.foregroundService = WalkingForegroundService(context!!)
            this.foregroundService?.startService(context!!, {
                this.stepSensor?.startListening();
                this.locationSensor?.startLocationUpdates();
            });
        }
        return type;
    }

    fun stopCurrentActivity(): Activity? {
        Log.d(TAG, "Current Activity: " + currentActivity?.type)//(currentActivity?.steps);
        if (currentActivity == null) return null
        currentActivity?.endDateTime = System.currentTimeMillis()

        this.foregroundService?.stopService(context!!, {
            stepSensor?.stopListening()
            locationSensor?.stopLocationUpdates()
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
        return "Steps: " + currentActivity?.steps.toString() + " Speed: " + ((currentActivity?.locations?.values?.last()?.speed?.times(
            3.6F
        )?.times(10.0))?.roundToInt()
            ?.div(10.0)).toString() + " km/h; Distance: " + (currentActivity?.distance).toString() + " km";
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

