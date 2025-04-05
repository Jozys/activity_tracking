package de.buseslaar.tracking.activity_tracking.model

enum class Event(val type: String) {
    STEP("step"),
    LOCATION("location"),
    DISTANCE("distance"),
    PAUSE("pause"),
    RESUME("resume"),
    STOP("stop"),
}