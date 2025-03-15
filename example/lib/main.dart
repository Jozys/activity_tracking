import 'package:activity_tracking/model/Activity.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:activity_tracking/activity_tracking.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String activityRunning = "Unknown";
  Activity? activity =
      Activity(activityType: "UNKNOWN", steps: 0, locations: {});
  final _activityTrackingPlugin = ActivityTracking();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> startTracking() async {
    String activity;
    try {
      activity =
          await _activityTrackingPlugin.startActivity("WALKING") ?? "UNKNOWN";
    } on Exception {
      activity = "Failed to start activity";
    }

    setState(() {
      activityRunning = activity;
    });
  }

  Future<void> stopTracking() async {
    Activity? newActivity;
    String? activityState;
    try {
      newActivity = await _activityTrackingPlugin.stopCurrentActivity();
      activityState = "Stopped";
    } on Exception {
      activityState = "Failed to stop";
      newActivity = Activity(activityType: "UNKNOWN", steps: -1, locations: {});
    }

    setState(() {
      activityRunning = activityState!;
      activity = Activity(
          activityType: newActivity?.activityType,
          locations: newActivity?.locations,
          steps: newActivity?.steps);
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _activityTrackingPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Text('Running on: $_platformVersion\n'),
            FilledButton(
                onPressed: startTracking, child: Text("Start Tracking")),
            FilledButton(onPressed: stopTracking, child: Text("StopTracking")),
            Text('Is Tracking running: ${activityRunning}'),
            Text('Activity: ${activity?.activityType}'),
            Text('Steps: ${activity?.steps}'),
            Text('Locations: ${activity?.locations?.length}')
          ],
        ),
      ),
    );
  }
}
