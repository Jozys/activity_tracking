import 'package:activity_tracking/model/Activity.dart';

import 'activity_tracking_platform_interface.dart';

class ActivityTracking {
  Future<String?> getPlatformVersion() {
    return ActivityTrackingPlatform.instance.getPlatformVersion();
  }

  Future<String?> startActivity(String type) {
    return ActivityTrackingPlatform.instance.startActivity(type);
  }

  Future<Activity?> stopCurrentActivity() {
    return ActivityTrackingPlatform.instance.stopCurrentActivity();
  }
}
