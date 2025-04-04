import 'dart:convert';

import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/event.dart';
import 'package:activity_tracking/model/location.dart';

class Message<T> {
  Event? type;
  T? data;

  Message({this.data, this.type});

  factory Message.fromJson(Map<String, dynamic> json) {
    var type = Event.fromString(json["type"]);
    print(json);
    switch (type) {
      case Event.step:
        return Message(type: type, data: json["data"]);
      case Event.location:
        var rawLocations = json["data"] as Map<String, dynamic>;
        var locations = <DateTime, Location>{};
        rawLocations.forEach((key, value) {
          locations[DateTime.fromMillisecondsSinceEpoch(int.parse(key))] =
              Location(
            latitude: value["latitude"],
            longitude: value["longitude"],
            altitude: value["altitude"],
            speed: value["speed"] * 1.0,
            pace: value["pace"] * 1.0,
          );
        });
        return Message(type: type, data: locations as T);
      case Event.pause:
      case Event.stop:
      case Event.resume:
        var activity = Activity.fromJson(jsonDecode(json["data"]));
        return Message(type: type, data: activity as T);
      default:
        return Message(type: type, data: json["data"]);
    }
  }
}
