package de.buseslaar.tracking.activity_tracking.activity

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.sensor.LocationSensor

class BikingActivity() : Activity(activityType = ActivityType.BIKING) {

    private var locationSensor: LocationSensor? = null
    private var onLocationChanged: (List<Location>) -> Unit = {}

    constructor(onLocationChanged: (List<Location>) -> Unit) : this() {
        this.onLocationChanged = onLocationChanged
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startSensors(context: Context) {
        locationSensor = LocationSensor(context, onLocationUpdatedListener = {
            onLocationChanged(it)
        })
        locationSensor?.startLocationUpdates()
    }

    override fun stopSensors(context: Context) {
        locationSensor?.stopLocationUpdates()
    }

}