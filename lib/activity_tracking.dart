
import 'activity_tracking_platform_interface.dart';

class ActivityTracking {
  Future<String?> getPlatformVersion() {
    return ActivityTrackingPlatform.instance.getPlatformVersion();
  }
}
