import 'dart:ffi';

import 'package:activity_tracking/model/Location.dart';

class Activity {
  // TODO: Ändere den Typ in Health Connect-Typen
  String? activityType;
  int? steps = 0;
  Map<DateTime, Location>? locations = {};

  Activity({this.activityType, this.steps, this.locations});

  factory Activity.fromJson(Map<String, dynamic> json) {
    var rawLocations = json["locations"] as Map<String, dynamic>;
    var locations = <DateTime, Location>{};
    rawLocations.forEach((key, value) {
      locations[DateTime.fromMillisecondsSinceEpoch(int.parse(key))] = Location(
          latitude: value["latitude"],
          longitude: value["longitude"],
          altitude: value["altitude"]);
    });
    return Activity(
        activityType: json["type"], locations: locations, steps: json["steps"]);
  }
}
