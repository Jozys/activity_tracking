import 'package:activity_tracking/model/location.dart';

class Message<T> {
  String? type;
  T? data;

  Message({this.data, this.type});

  factory Message.fromJson(Map<String, dynamic> json) {
    var type = json["type"];
    switch (type) {
      case "step":
        return Message(type: type, data: json["data"]);
      case "location":
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
      default:
        return Message(type: type, data: json["data"]);
    }
  }
}
