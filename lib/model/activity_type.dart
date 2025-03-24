enum ActivityType {
  walking(name: "WALKING"),
  running(name: "RUNNING"),
  cycling(name: "CYCLING");

  const ActivityType({required this.name});

  final String name;
}
