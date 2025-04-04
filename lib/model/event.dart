enum Event {
  step(name: "step"),
  location(name: "location"),
  distance(name: "distance"),
  pause(name: "pause"),
  resume(name: "resume"),
  stop(name: "stop");

  const Event({required this.name});
  final String name;

  getName() {
    return name;
  }

  static Event fromString(String name) {
    return Event.values
        .firstWhere((e) => e.name == name, orElse: () => Event.step);
  }
}
