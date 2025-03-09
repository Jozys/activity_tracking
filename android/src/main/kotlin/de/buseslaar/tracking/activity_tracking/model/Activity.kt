package de.buseslaar.tracking.activity_tracking.model

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.impl.HealthConnectClientImpl

class Activity {

    // TODO: Change type to Health Connect types
    private var type : String?
        get() = type
        set(value) {
            if(value?.isNotEmpty() == true) {
                type = value
            }
        }

    private var steps: Int
        get() = steps
        set(value) {
            if(value != 0) {
                steps = value;
            }
        }

    constructor(activityType: String) {
        type = activityType;
        steps = 0;
    }
}