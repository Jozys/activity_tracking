import 'dart:ffi';

import 'package:activity_tracking/model/Activity.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'activity_tracking_platform_interface.dart';

/// An implementation of [ActivityTrackingPlatform] that uses method channels.
class MethodChannelActivityTracking extends ActivityTrackingPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('activity_tracking');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> startActivity(String type) async {
    final started = await methodChannel.invokeMethod<String>('startActivity', <String, dynamic> {
      'type': type
    });
    return started;
  }

  @override
  Future<Activity?> stopCurrentActivity() async {
    final stopped = await methodChannel.invokeMethod<Map<Object?, Object?>>('stopCurrentActivity');
    var type = stopped?.entries.firstWhere((element) => element.key == "type").value as String;
    var steps = stopped?.entries.firstWhere((element) => element.key == "steps").value as String;
    if(type != null || steps != null)
      return Activity(activityType: type, steps: int.parse(steps));
  }
}
