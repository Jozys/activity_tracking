import 'dart:convert';

import 'package:activity_tracking/model/Location.dart';
import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/message.dart';
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
  List<Activity> activities = <Activity>[];
  StreamSubscription? listener;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> startTracking() async {
    String activityType;
    try {
      activityType =
          await _activityTrackingPlugin.startActivity("WALKING") ?? "UNKNOWN";
      setState(() {
        listener = _activityTrackingPlugin
            .getNativeEvents()
            .listen(eventMessageListener);
      });
    } on Exception {
      activityType = "UNKNOWN";
    }
    activity?.activityType = activityType;

    setState(() {
      activityRunning = activityType != "UNKNOWN" ? "Yes" : "No";
      activity = activity;
    });
  }

  void eventMessageListener(dynamic e) {
    var eventMessage = Message.fromJson(jsonDecode(e));
    switch (eventMessage.type) {
      case "step":
        activity?.steps =
            ((activity?.steps ?? 0) + (eventMessage.data ?? 0)) as int?;
      case "location":
        if (eventMessage.data != null) {
          activity?.locations?.addAll(eventMessage.data);
        }
    }
    setState(() {
      activity = activity;
    });
  }

  Future<void> stopTracking() async {
    Activity? newActivity;
    String? activityState;
    List<Activity> newActivities = activities;
    try {
      newActivity = await _activityTrackingPlugin.stopCurrentActivity();
      if (newActivity != null) {
        newActivities.add(newActivity);
      }

      activityState = "Stopped";
    } on Exception {
      activityState = "Failed to stop";
      newActivity = Activity(activityType: "UNKNOWN", steps: -1, locations: {});
    }
    listener?.cancel();

    setState(() {
      activityRunning = activityState!;
      activity = Activity(activityType: "UNKNOWN", steps: 0, locations: {});
      listener = null;
      activities = newActivities;
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
            actions: [
              FilledButton(
                  onPressed: startTracking,
                  child: const Text("Start Tracking")),
              FilledButton(
                  onPressed: stopTracking, child: const Text("StopTracking")),
            ],
          ),
          body: Container(
            child: Column(
              children: [
                Expanded(child: _buildList(context, activities)),
                Row(children: [
                  Column(children: [
                    Text('Is Tracking running: ${activityRunning}'),
                    Text('Activity: ${activity?.activityType}'),
                    Text('Steps: ${activity?.steps}'),
                    Text('Locations: ${activity?.locations?.length}'),
                  ]),
                ]),
              ],
            ),
          )),
    );
  }

  Widget _buildList(BuildContext context, List<Activity> activities) {
    return RefreshIndicator(
        color: Colors.white,
        backgroundColor: Colors.blue,
        onRefresh: () async {},
        child: activities.isNotEmpty
            ? ListView.builder(
                itemCount: activities.length,
                itemBuilder: (context, index) {
                  final activity = activities[index];
                  return Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8.0),
                    child: _buildListItem(context, activity),
                  );
                })
            : const Text("Hallo Welt"));
  }

  Widget _buildListItem(BuildContext context, Activity activity) {
    return ListTile(
      minTileHeight: 120,
      title: Text("Activity type: ${activity.activityType ?? "UNKNOWN"}"),
      isThreeLine: true,
      subtitle: Column(children: [
        Text(DateTime.fromMillisecondsSinceEpoch(activity.startDateTime ?? 0)
            .toIso8601String()),
        Text(DateTime.fromMillisecondsSinceEpoch(activity.endDateTime ?? 0)
            .toIso8601String()),
        Text("Activity type: ${activity.activityType ?? "UNKNOWN"}"),
        Text("Steps ${activity.steps.toString()}"),
        Text("Locations: ${activity.locations?.length}")
      ]),
      trailing: CircleAvatar(
          backgroundColor: Theme.of(context).colorScheme.primary,
          child: Text("Hallo Welt"),
          foregroundColor: Theme.of(context).colorScheme.onPrimary),
    );
  }
}
