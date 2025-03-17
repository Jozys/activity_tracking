package de.buseslaar.tracking.activity_tracking.model

class Activity {

    // TODO: Change type to Health Connect types
    var type: String? = null

    var steps: Int = 0

    var locations: HashMap<Long, Location> = HashMap()

    var startDateTime: Long? = null;

    var endDateTime: Long? = null;

    constructor(activityType: String) {
        type = activityType
        steps = 0
        startDateTime = System.currentTimeMillis()
    }

    fun addLocation(millis: Long, location: Location) {
        locations.put(millis, location)
    }

    fun parseToJSON(): String {
        var json = """
            {
                "startDateTime": $startDateTime,
                "endDateTime": $endDateTime,
                "type": "$type",
                "steps": $steps,
                "locations": {${parseHashMapToJson()}}}
        """.trimIndent()
        return json
    }

    private fun parseHashMapToJson(): String {
        var json = ""
        if (locations.isEmpty()) return json
        locations.forEach { it ->
            json =
                json + """"${it.key}": ${it.value.parseToJSON()}${if (it.key != locations.keys.last()) "," else ""}"""
        }
        return json
    }
}

