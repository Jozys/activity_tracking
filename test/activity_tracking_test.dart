import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/activity_type.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:activity_tracking/activity_tracking.dart';
import 'package:activity_tracking/activity_tracking_platform_interface.dart';
import 'package:activity_tracking/activity_tracking_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockActivityTrackingPlatform
    with MockPlatformInterfaceMixin
    implements ActivityTrackingPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String?> startActivity(ActivityType type) {
    throw UnimplementedError();
  }

  @override
  Future<Activity> stopCurrentActivity() {
    throw UnimplementedError();
  }

  @override
  Stream getNativeEvents() {
    throw UnimplementedError();
  }
}

void main() {
  final ActivityTrackingPlatform initialPlatform =
      ActivityTrackingPlatform.instance;

  test('$MethodChannelActivityTracking is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelActivityTracking>());
  });

  test('getPlatformVersion', () async {
    ActivityTracking activityTrackingPlugin = ActivityTracking();
    MockActivityTrackingPlatform fakePlatform = MockActivityTrackingPlatform();
    ActivityTrackingPlatform.instance = fakePlatform;

    expect(await activityTrackingPlugin.getPlatformVersion(), '42');
  });
}
