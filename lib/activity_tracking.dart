import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/activity_type.dart';

import 'activity_tracking_platform_interface.dart';

class ActivityTracking {
  Future<String?> getPlatformVersion() {
    return ActivityTrackingPlatform.instance.getPlatformVersion();
  }

  Future<Activity?> startActivity(ActivityType type) {
    return ActivityTrackingPlatform.instance.startActivity(type);
  }

  Future<Activity?> stopCurrentActivity() {
    return ActivityTrackingPlatform.instance.stopCurrentActivity();
  }

  Stream<dynamic> getNativeEvents() {
    return ActivityTrackingPlatform.instance.getNativeEvents();
  }
}
