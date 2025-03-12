package de.buseslaar.tracking.activity_tracking.activitymanager


import android.content.Context
import android.util.Log
import de.buseslaar.tracking.activity_tracking.model.Activity
import de.buseslaar.tracking.activity_tracking.sensor.StepSensor

private const val TAG = "ACTIVITY_MANAGER"

class ActivityManager {

    private var currentActivity: Activity? = null
    private var stepSensor: StepSensor? = null

    constructor(newContext: Context) {
        stepSensor = StepSensor(newContext, onStepChanged = {
            onStepChanged(it)
        })

    }

    fun startActivity(type: String) {
        currentActivity = Activity(type);
        stepSensor?.startListening()
        Log.d(TAG, "Current Activity: $currentActivity");
    }

    fun stopCurrentActivity(): Activity? {
        Log.d(TAG, "Steps: " + currentActivity?.type);//(currentActivity?.steps);
        if (currentActivity == null) return null;
        stepSensor?.stopListening();
        return currentActivity;
    }

    fun onStepChanged(addedSteps: Int) {
        currentActivity?.steps = currentActivity?.steps?.plus(addedSteps)!!;
        Log.d(TAG, "Steps: " + currentActivity?.steps);
    }


}