package de.buseslaar.tracking.activity_tracking.activity

import android.location.Location
import de.buseslaar.tracking.activity_tracking.model.ActivityType

class RunningActivity : WalkingActivity {

    constructor() : super(
        activityType = ActivityType.RUNNING,
        onLocationChanged = {},
        onStepChanged = {})

    constructor(
        onLocationChanged: (List<Location>) -> Unit,
        onStepChanged: (Int) -> Unit
    ) : super(activityType = ActivityType.RUNNING, onLocationChanged, onStepChanged) {
    }


}