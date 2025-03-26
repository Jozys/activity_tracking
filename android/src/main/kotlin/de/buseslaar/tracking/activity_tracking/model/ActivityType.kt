package de.buseslaar.tracking.activity_tracking.model

enum class ActivityType(var type: String) {
    WALKING("Walking"),
    RUNNING("Running"),
    BIKING("Biking"),
    UNKNOWN("Unknown")
}