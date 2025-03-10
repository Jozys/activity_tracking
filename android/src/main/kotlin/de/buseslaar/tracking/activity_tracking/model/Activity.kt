package de.buseslaar.tracking.activity_tracking.model

class Activity {

    // TODO: Change type to Health Connect types
    var type : String? = null

    var steps: Int

    constructor(activityType: String) {
        type = activityType;
        steps = 0;
    }
}

