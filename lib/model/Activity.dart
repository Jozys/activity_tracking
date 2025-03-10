class Activity {
  // TODO: Ändere den Typ in Health Connect-Typen
  String? type;
  int steps;


  Activity({required String activityType, required int steps})
      : type = activityType,
        steps = steps;
}