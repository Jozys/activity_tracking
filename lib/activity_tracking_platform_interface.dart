import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/activity_type.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'activity_tracking_method_channel.dart';

abstract class ActivityTrackingPlatform extends PlatformInterface {
  /// Constructs a ActivityTrackingPlatform.
  ActivityTrackingPlatform() : super(token: _token);

  static final Object _token = Object();

  static ActivityTrackingPlatform _instance = MethodChannelActivityTracking();

  /// The default instance of [ActivityTrackingPlatform] to use.
  ///
  /// Defaults to [MethodChannelActivityTracking].
  static ActivityTrackingPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ActivityTrackingPlatform] when
  /// they register themselves.
  static set instance(ActivityTrackingPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> startActivity(ActivityType type) {
    throw UnimplementedError("startActivity() has not been implemented");
  }

  Future<Activity?> stopCurrentActivity() {
    throw UnimplementedError("stopCurrentActivity has not been implemented");
  }

  Stream<dynamic> getNativeEvents() {
    throw UnimplementedError("getNativeEvents has not been implemented");
  }
}
