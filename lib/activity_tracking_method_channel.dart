import 'dart:convert';

import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/activity_type.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'activity_tracking_platform_interface.dart';

/// An implementation of [ActivityTrackingPlatform] that uses method channels.
class MethodChannelActivityTracking extends ActivityTrackingPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('activity_tracking');
  final _eventChannel = const EventChannel("activity_tracking/channel");

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>(
      'getPlatformVersion',
    );
    return version;
  }

  @override
  Future<Activity?> startActivity(ActivityType type) async {
    final started = await methodChannel.invokeMethod<String>(
      'startActivity',
      <String, dynamic>{'type': type.name},
    );

    return Activity.fromJson(jsonDecode(started!));
  }

  @override
  Future<Activity?> stopCurrentActivity() async {
    final stopped = await methodChannel.invokeMethod<String>(
      'stopCurrentActivity',
    );
    try {
      if (stopped != null && stopped.isNotEmpty) {
        var activity = Activity.fromJson(jsonDecode(stopped));
        return activity;
      }
    } catch (e) {
      return Activity(activityType: ActivityType.unknown);
    }
    return null;
  }

  @override
  Future<bool?> togglePauseActivity() async {
    return await methodChannel.invokeMethod<bool?>('togglePauseActivity');
  }

  @override
  Stream<dynamic> getNativeEvents() {
    return _eventChannel.receiveBroadcastStream();
  }
}
