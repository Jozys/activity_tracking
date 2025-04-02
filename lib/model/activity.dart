import 'dart:ffi';

import 'package:activity_tracking/model/location.dart';
import 'package:activity_tracking/model/activity_type.dart';

class Activity {
  // TODO: Ändere den Typ in Health Connect-Typen
  ActivityType? activityType;
  int? steps = 0;
  Map<DateTime, Location>? locations = {};
  int? startDateTime = 0;
  int? endDateTime = 0;
  double? distance = 0.0;

  Activity(
      {this.activityType,
      this.steps,
      this.locations,
      this.startDateTime,
      this.endDateTime,
      this.distance});

  factory Activity.fromJson(Map<String, dynamic> json) {
    var rawLocations = json["locations"] as Map<String, dynamic> ?? {};
    var locations = <DateTime, Location>{};
    rawLocations.forEach((key, value) {
      locations[DateTime.fromMillisecondsSinceEpoch(int.parse(key))] = Location(
          latitude: value["latitude"],
          longitude: value["longitude"],
          altitude: value["altitude"],
          speed: value["speed"],
          pace: value["pace"]);
    });
    return Activity(
        startDateTime: json["startDateTime"] ?? 0,
        endDateTime: json["endDateTime"] ?? 0,
        activityType:
            ActivityType.values.firstWhere((e) => e.name == json["type"]),
        distance: json["distance"] ?? 0.0,
        locations: locations ?? {},
        steps: json["steps"] ?? 0);
  }
}
