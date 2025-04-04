package de.buseslaar.tracking.activity_tracking.model

import android.location.Location
import kotlin.math.roundToInt

class Location {

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0
    var speed: Float = 0.0F
    var pace: Float = 0.0F

    constructor(latitude: Double, longitude: Double, altitude: Double, speed: Float) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.speed = toKiloMetersPerHour(speed)
        this.pace = speedToPace(speed)
    }

    fun parseToJSON(): String {
        return """
            {
                "latitude": $latitude,
                "longitude": $longitude,
                "altitude": $altitude,
                "speed": ${speed.toDouble()},
                "pace": ${
            pace.toDouble()
        }}"""
    }

    companion object {
        fun toKiloMetersPerHour(speed: Float): Float {
            return speed.times(3.6F).times(10.0).roundToInt().div(10.0).toFloat()
        }

        fun kilometersPerHourToMetersPerSecond(kilometersPerHour: Float): Float {
            return kilometersPerHour.div(3.6F)
        }

    }

    private fun speedToPace(speed: Float): Float {
        var pace = 0.0F;
        if (speed <= 0.0) {
            pace = 0.0F
            return pace
        }
        // Convert speed from m/s to km/h first
        val speedKmh = toKiloMetersPerHour(speed)
        if (speedKmh <= 0.0) {
            pace = 0.0F
            return pace
        }
        // Calculate pace as seconds per kilometer (60 * 60 / speedKmh)
        pace = 3600.0F / speedKmh
        return pace;
    }

    fun distanceTo(compare: Location): Float {
        var location = Location("");
        location.latitude = this.latitude;
        location.longitude = this.longitude;
        location.altitude = this.altitude;
        location.speed = kilometersPerHourToMetersPerSecond(speed);
        // Distance in meters
        var distance = location.distanceTo(compare) / 1000;
        return distance;
    }

}