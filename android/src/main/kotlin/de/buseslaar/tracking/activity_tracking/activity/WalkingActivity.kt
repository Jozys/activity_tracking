package de.buseslaar.tracking.activity_tracking.activity

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.model.ActivityType
import de.buseslaar.tracking.activity_tracking.sensor.LocationSensor
import de.buseslaar.tracking.activity_tracking.sensor.StepSensor

open class WalkingActivity() : Activity(activityType = ActivityType.WALKING) {

    private var locationSensor: LocationSensor? = null
    private var stepSensor: StepSensor? = null
    internal var onLocationChanged: (List<Location>) -> Unit = {}
    internal var onStepChanged: (Int) -> Unit = {}

    constructor(
        onLocationChanged: (List<Location>) -> Unit,
        onStepChanged: (Int) -> Unit
    ) : this() {
        this.onLocationChanged = onLocationChanged
        this.onStepChanged = onStepChanged
    }

    constructor(
        activityType: ActivityType,
        onLocationChanged: (List<Location>) -> Unit,
        onStepChanged: (Int) -> Unit
    ) : this() {
        super.type = activityType
        this.onLocationChanged = onLocationChanged
        this.onStepChanged = onStepChanged
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startSensors(context: Context) {
        locationSensor = LocationSensor(context, onLocationUpdatedListener = {
            onLocationChanged(it)
        })
        stepSensor = StepSensor(context, onStepChanged = {
            onStepChanged(it)
        })
        locationSensor?.startLocationUpdates()
        stepSensor?.startListening()
    }

    override fun stopSensors(context: Context) {
        locationSensor?.stopLocationUpdates()
        stepSensor?.stopListening()
    }

}