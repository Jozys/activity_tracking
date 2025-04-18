import 'dart:convert';

import 'package:activity_tracking/model/event.dart';
import 'package:activity_tracking/model/location.dart';
import 'package:activity_tracking/model/activity.dart';
import 'package:activity_tracking/model/activity_type.dart';
import 'package:activity_tracking/model/message.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:activity_tracking/activity_tracking.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String activityRunning = "Unknown";
  Activity? activity = Activity(
      activityType: ActivityType.unknown,
      steps: 0,
      locations: {},
      distance: 0.0);
  final _activityTrackingPlugin = ActivityTracking();
  List<Activity> activities = <Activity>[];
  bool isRecording = false;
  StreamSubscription? listener;

  @override
  void initState() {
    super.initState();
  }

  Future<bool> checkPermission() async {
    Map<Permission, PermissionStatus> perms = await [
      Permission.location,
      Permission.activityRecognition,
      Permission.locationWhenInUse,
      Permission.notification,
    ].request();
    var success = true;
    for (var val in perms.entries) {
      if (val.value.isDenied) {
        print("Permission denied for ${val.key}");
        success = false;
      }
    }
    return success;
  }

  Future<void> startTracking(ActivityType type) async {
    if (!(await checkPermission())) return;
    String activityType;
    Activity? startedActivity;

    try {
      startedActivity = await _activityTrackingPlugin.startActivity(type);
      activityType = activity?.activityType?.name ?? "UNKNOWN";

      setState(() {
        listener = _activityTrackingPlugin
            .getNativeEvents()
            .listen(eventMessageListener);
      });
    } on Exception {
      activityType = "UNKNOWN";
    }
    activity?.activityType =
        ActivityType.values.firstWhere((e) => e.name == activityType);
    setState(() {
      activityRunning = activityType != "UNKNOWN" ? "Yes" : "No";
      activity = startedActivity;
      isRecording = true;
    });
  }

  void eventMessageListener(dynamic e) {
    var eventMessage = Message.fromJson(jsonDecode(e));
    if (eventMessage.type == null) return;

    switch (eventMessage.type!) {
      case Event.step:
        activity?.steps =
            ((activity?.steps ?? 0) + (eventMessage.data ?? 0)) as int?;
      case Event.location:
        if (eventMessage.data != null) {
          activity?.locations?.addAll(eventMessage.data);
        }
      case Event.distance:
        if (eventMessage.data != null && eventMessage.data != 0) {
          activity?.distance = eventMessage.data;
        }
      case Event.pause:
        isRecording = !isRecording;
        activity = eventMessage.data as Activity;
      case Event.resume:
        isRecording = !isRecording;
        activity = eventMessage.data as Activity;

      case Event.stop:
        isRecording = false;
        if (eventMessage.data is Activity) {
          activities.add(eventMessage.data as Activity);
        }
        activity = Activity(
            activityType: ActivityType.unknown,
            steps: 0,
            locations: {},
            distance: 0.0);
    }
    setState(() {
      activity = activity;
      isRecording = isRecording;
      activities = activities;
      activityRunning = activity?.activityType?.name ?? "UNKNOWN";
    });
  }

  Future<void> pauseTracking() async {
    String? activityState;
    try {
      var success = await _activityTrackingPlugin.togglePauseActivity();
      if (success != null && success == true) {
        activityState = "Paused";
        isRecording = !isRecording;
      }
    } on Exception {
      activityState = "Failed to pause";
    }
    setState(() {
      activityRunning = activityState!;
      isRecording = isRecording;
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
      newActivity = Activity(
          activityType: ActivityType.unknown,
          steps: -1,
          locations: {},
          distance: 0.0);
    }
    listener?.cancel();

    setState(() {
      activityRunning = activityState!;
      activity = Activity(
          activityType: ActivityType.unknown,
          steps: 0,
          locations: {},
          distance: 0.0);
      listener = null;
      activities = newActivities;
    });
  }

  double calculateAverageSpeed(Map<DateTime, Location> locations) {
    double sum = 0.0;

    if (locations.length == 0) return 0;

    locations.forEach((datetime, location) {
      sum += location.speed;
    });

    return ((sum / locations.length) * 10.0).round() / 10.0;
  }

  String getPace(double pace) {
    if (pace == 0) return "0:00 min/km";
    var minutes = pace ~/ 60;
    var seconds = (pace % 60).toInt();
    return "${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')} min/km";
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
            actions: [
              FilledButton(
                  onPressed: activity?.activityType != ActivityType.unknown
                      ? stopTracking
                      : null,
                  child: const Text("StopTracking")),
            ],
          ),
          body: Container(
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    FilledButton(
                      onPressed: activity?.activityType == ActivityType.unknown
                          ? () => startTracking(ActivityType.running)
                          : null,
                      child: const Text("Running"),
                    ),
                    FilledButton(
                      onPressed: activity?.activityType == ActivityType.unknown
                          ? () => startTracking(ActivityType.walking)
                          : null,
                      child: const Text("Walking"),
                    ),
                    FilledButton(
                      onPressed: activity?.activityType == ActivityType.unknown
                          ? () => startTracking(ActivityType.biking)
                          : null,
                      child: const Text("Biking"),
                    ),
                    FilledButton(
                        onPressed:
                            activity?.activityType != ActivityType.unknown
                                ? () => pauseTracking()
                                : null,
                        child: Text(isRecording ? "Pause" : "Resume")),
                  ],
                ),
                const Divider(),
                Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                  Column(children: [
                    Text('Is Tracking running: $activityRunning'),
                    Text('Activity: ${activity?.activityType}'),
                    Text('Steps: ${activity?.steps}'),
                    Text('Locations: ${activity?.locations?.length}'),
                    if (activity?.locations != null &&
                        activity!.locations!.isNotEmpty)
                      Column(
                        children: [
                          Text(
                              'Current speed: ${activity?.locations?.entries?.last?.value?.speed}'),
                          Text(
                              'Current pace: ${getPace(activity?.locations?.entries?.last?.value?.pace ?? 0)}'),
                        ],
                      ),
                    Text('Distance: ${activity?.distance?.toString()}'),
                    Text(
                        'Average Speed: ${calculateAverageSpeed(activity?.locations ?? {})}'),
                    Text("Is Recording: $isRecording"),
                  ]),
                ]),
                const Divider(),
                Expanded(child: _buildList(context, activities)),
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
            : const Text("No activities record"));
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
        Text("Locations: ${activity.locations?.length}"),
        Text(
            "Average Speed:  ${calculateAverageSpeed(activity.locations ?? {})} km/h"),
        Text("Distance: ${activity.distance} km")
      ]),
      trailing: CircleAvatar(
          backgroundColor: Theme.of(context).colorScheme.primary,
          child: Text("Hallo Welt"),
          foregroundColor: Theme.of(context).colorScheme.onPrimary),
    );
  }
}
