package de.buseslaar.tracking.activity_tracking.model

import android.location.Location

class Location {

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0
    var speed: Float = 0.0F

    constructor(latitude: Double, longitude: Double, altitude: Double, speed: Float) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.speed = speed
    }

    fun parseToJSON(): String {
        return """
            {
                "latitude": $latitude,
                "longitude": $longitude,
                "altitude": $altitude,
                "speed": ${speed.toDouble()}
            }"""
    }

    fun distanceTo(compare: Location): Float {
        var location = Location("");
        location.latitude = this.latitude;
        location.longitude = this.longitude;
        location.altitude = this.altitude;
        location.speed = this.speed;
        // Distance in meters
        var distance = location.distanceTo(compare) / 1000;
        return distance;
    }

}