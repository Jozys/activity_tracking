enum ActivityType {
  walking(name: "WALKING"),
  running(name: "RUNNING"),
  cycling(name: "CYCLING"),
  unknown(name: "UNKNOWN");

  const ActivityType({required this.name});

  final String name;
}
