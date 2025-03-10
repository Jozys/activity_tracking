package de.buseslaar.tracking.activity_tracking.activitymanager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.layout.Column


import de.buseslaar.tracking.activity_tracking.model.Activity
import kotlinx.coroutines.suspendCancellableCoroutine
private const val TAG = "STEP_COUNT_LISTENER"
class ActivityManager {

    private var sensorManager : SensorManager? = null
    private var sensor: Sensor? = null
    private var currentActivity: Activity? = null

    constructor(newContext: Context) {
        // = newContext
        sensorManager = newContext.getSystemService(Context.SENSOR_SERVICE ) as SensorManager;
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }
    fun startActivity(type: String) {
        currentActivity = Activity(type);
        Log.d(TAG, "Current Activity: $currentActivity");
        val supportedAndEnabled = sensorManager?.registerListener(listener,
            sensor, SensorManager.SENSOR_DELAY_UI)
        Log.d(TAG, "Sensor listener registered: $supportedAndEnabled")
    }

    fun stopCurrentActivity(): Activity? {
        Log.d(TAG, "Steps: " + currentActivity?.type);//(currentActivity?.steps);
        if(currentActivity == null) return null;
        sensorManager?.unregisterListener(listener)
        return currentActivity;
    }

    private val listener: SensorEventListener by lazy {
        object : SensorEventListener {
              override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return

                    val stepsSinceLastReboot = event.values
                    for (step in stepsSinceLastReboot) {
                        Log.d(TAG, "Steps: $step")
                        if(currentActivity != null) {
                            currentActivity?.steps = currentActivity?.steps?.plus(1)!!
                        }
                    }
                    Log.d(TAG, "Steps since last reboot: $stepsSinceLastReboot")

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Log.d(TAG, "Accuracy changed to: $accuracy")
                }
            }
        }



}