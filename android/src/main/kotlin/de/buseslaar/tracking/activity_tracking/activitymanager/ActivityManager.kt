package de.buseslaar.tracking.activity_tracking.activitymanager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorListener
import android.hardware.SensorManager
import android.util.Log


import de.buseslaar.tracking.activity_tracking.model.Activity
import kotlinx.coroutines.suspendCancellableCoroutine
private const val TAG = "STEP_COUNT_LISTENER"
class ActivityManager {

    private var context : Context?
        get() {
            TODO()
        }
        set(value)  {
            if(value != null )  {
                context = value
            }
        }

    private val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val currentActivity: Activity?
        get() = currentActivity;

    constructor(newContext: Context) {
        context = newContext
    }

    suspend fun steps() = suspendCancellableCoroutine<Long> { continuation ->
        Log.d(TAG, "Registering sensor listener... ")

        val listener: SensorEventListener by lazy {
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return

                    val stepsSinceLastReboot = event.values[0].toLong()
                    Log.d(TAG, "Steps since last reboot: $stepsSinceLastReboot")

                    if (continuation.isActive) {
                        continuation.resume(value = stepsSinceLastReboot, onCancellation = {
                            print("cancelled")
                        })
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Log.d(TAG, "Accuracy changed to: $accuracy")
                }
            }
        }

        val supportedAndEnabled = sensorManager.registerListener(listener,
            sensor, SensorManager.SENSOR_DELAY_UI)
        Log.d(TAG, "Sensor listener registered: $supportedAndEnabled")
    }

}