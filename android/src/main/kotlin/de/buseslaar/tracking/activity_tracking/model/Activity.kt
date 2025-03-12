package de.buseslaar.tracking.activity_tracking.model

import java.util.Date

class Activity {

    // TODO: Change type to Health Connect types
    var type : String? = null

    var steps : Int = 0;

    var gpsData : Map<Date, Location> = HashMap();

    constructor(activityType: String) {
        type = activityType;
        steps = 0;
    }

    fun addLocation(location: Location) {
        gpsData.plus(Pair(Date(), location));
    }
}

