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
    fun startActivity(type: String) {
        currentActivity = Activity(type)
        stepSensor?.startListening()
        locationSensor?.startLocationUpdates()
        Log.d(TAG, "Current Activity: $currentActivity")
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
        eventSink?.success(currentActivity?.parseToJSON());
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
        }
        Log.d("ACTIVITY_MANAGER", "Current Activity: $currentActivity")
        Log.d("ACTIVITY_MANAGER", "Current Activity: $eventSink")
        eventSink?.success(currentActivity?.parseToJSON());
    }

}