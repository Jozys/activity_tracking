package de.buseslaar.tracking.activity_tracking.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent


class StepSensor : SensorManager {

    private val TAG = "StepSensor"
    private var onStepChangedListener: (addedSteps: Int) -> Unit = {}

    init {
        super.onSensorChangedListener = ({ onSensorChangedListener(it) })
    }

    constructor(context: Context, onStepChanged: (addedSteps: Int) -> Unit) : super(
        context,
        Sensor.TYPE_STEP_DETECTOR
    ) {
        this.onStepChangedListener = onStepChanged
    }

    private fun onSensorChangedListener(event: SensorEvent) {
        val stepsSinceLastReboot = event.values
        onStepChangedListener(stepsSinceLastReboot[0].toInt())
    }
}