package de.buseslaar.tracking.activity_tracking.model

import de.buseslaar.tracking.activity_tracking.R

enum class ActivityType(var type: String, var resourceId: Int) {
    WALKING("Walking", R.string.WALKING),
    RUNNING("Running", R.string.RUNNING),
    BIKING("Biking", R.string.BIKING),
    UNKNOWN("Unknown", R.string.UNKNOWN)
}