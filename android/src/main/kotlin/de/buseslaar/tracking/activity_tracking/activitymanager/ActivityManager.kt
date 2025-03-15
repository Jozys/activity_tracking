package de.buseslaar.tracking.activity_tracking.activitymanager


import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.sensor.LocationSensor
import de.buseslaar.tracking.activity_tracking.sensor.StepSensor
import io.flutter.plugin.common.EventChannel
import org.json.JSONObject

private const val TAG = "ACTIVITY_MANAGER"

class ActivityManager {

    private var currentActivity: Activity? = null
    private var stepSensor: StepSensor? = null
    private var locationSensor: LocationSensor? = null
    var eventSink: EventChannel.EventSink? = null

    constructor(newContext: Context) {
        this.stepSensor = StepSensor(newContext, onStepChanged = {
            onStepChanged(it)
        })
        this.locationSensor = LocationSensor(newContext, onLocationUpdatedListener = {
            onLocationChanged(it)
        })

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startActivity(type: String): String {
        currentActivity = Activity(type)
        stepSensor?.startListening()
        locationSensor?.startLocationUpdates()
        return type;
    }

    fun stopCurrentActivity(): Activity? {
        Log.d(TAG, "Steps: " + currentActivity?.type)//(currentActivity?.steps);
        if (currentActivity == null) return null
        currentActivity?.endDateTime = System.currentTimeMillis()
        stepSensor?.stopListening()
        locationSensor?.stopLocationUpdates()
        return currentActivity
    }

    fun onStepChanged(addedSteps: Int) {
        currentActivity?.steps = currentActivity?.steps?.plus(addedSteps)!!
        Log.d(TAG, "Steps: " + currentActivity?.steps)
        eventSink?.success(constructJsonString<Int>("step", addedSteps));
    }

    fun onLocationChanged(locations: List<Location>) {
        for (location in locations) {
            Log.d(TAG, location.longitude.toString())

            currentActivity?.addLocation(
                location.time,
                de.buseslaar.tracking.activity_tracking.model.Location(
                    location.latitude,
                    location.longitude,
                    location.altitude
                )
            )
            eventSink?.success(constructJsonString<Location>("location", location));
        }
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
                val locationTime = JSONObject();
                locationTime.put(rawLocationData.time.toString(), locationData);
                json.put("data", locationTime);
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