package de.buseslaar.tracking.activity_tracking.model

class Location {

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0

    constructor(latitude: Double, longitude: Double, altitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
    }

    fun parseToJSON(): String {
        return """
            {
                "latitude": $latitude,
                "longitude": $longitude,
                "altitude": $altitude
            }"""
    }

}