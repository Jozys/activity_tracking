package de.buseslaar.tracking.activity_tracking.activity

import android.location.Location
import de.buseslaar.tracking.activity_tracking.model.ActivityType

class RunningActivity : WalkingActivity {

    constructor(
        onLocationChanged: (List<Location>) -> Unit,
        onStepChanged: (Int) -> Unit
    ) : super(activityType = ActivityType.RUNNING, onLocationChanged, onStepChanged) {
    }

}