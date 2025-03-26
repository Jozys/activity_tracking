enum ActivityType {
  walking(name: "WALKING"),
  running(name: "RUNNING"),
  biking(name: "BIKING"),
  unknown(name: "UNKNOWN");

  const ActivityType({required this.name});

  final String name;
}
