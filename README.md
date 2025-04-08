# Activity Tracking Plugin

A Flutter plugin for tracking physical activities such as walking, running, and biking. Currently, it only supports Android devices.

## Features

- Track different activity types (walking, running, biking)
- Real-time step counting for walking and running
- GPS location tracking with distance calculation
- Speed and pace monitoring
- Foreground service with notification controls (pause/resume/stop)
- Event channel for real-time updates

## Getting Started

### Installation

Add the plugin to your `pubspec.yaml`:

```yaml
dependencies:
  activity_tracking:
    git:
      url: https://github.com/Jozys/activity_tracking.git
      ref: main
```

### Setup

#### Permissions

Add the following permissions to your Android manifest:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

For Android 10+ (API level 29+), also add:

```xml
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
```

#### Services and Receivers

Register the foreground service and broadcast receiver in your `AndroidManifest.xml`:

```xml
<application>
   <!-- Other application tags -->
   <service
        android:name="de.buseslaar.tracking.activity_tracking.service.ForegroundService"
        android:exported="false"
        android:foregroundServiceType="location|health" />
   <service
        android:name="de.buseslaar.tracking.activity_tracking.service.WalkingForegroundService"
        android:exported="false"
        android:foregroundServiceType="location|health" />
    <service
        android:name="de.buseslaar.tracking.activity_tracking.service.BikingForegroundService"
        android:exported="false"
        android:foregroundServiceType="location|health" />
    <service
    	android:name="de.buseslaar.tracking.activity_tracking.service.RunningForegroundService"
        android:exported="false"
        android:foregroundServiceType="location|health" />

</application>
```

#### Language Support

Create a `resource.properties` file in `android/app/src/main/res/` with the following content:

```properties
unqualifiedResLocale=en-US
```

Update your `android/app/build.gradle` to include the `androidResource` and `generateLocaleConfig` settings:

```groovy
android {
	// Other configurations
   androidResources {
       generateLocaleConfig = true
   }
}
```

### Basic Usage

```dart
import 'package:activity_tracking/activity_tracking.dart';

// Request required permissions before using the plugin

// Start tracking an activity
final activityType = await ActivityTracking.startActivity(ActivityType.WALKING);

// Listen for activity updates
ActivityTracking.getNativeEvents().listen((event) {
  if (event.type == EventType.STEP) {
    print('Steps: ${event.data}');
  } else if (event.type == EventType.LOCATION) {
    print('Location: ${event.data}');
  } else if (event.type == EventType.DISTANCE) {
    print('Distance: ${event.data} km');
  }
});

// Pause/resume the current activity
await ActivityTracking.pauseCurrentActivity();

// Stop the current activity and get the summary
final activitySummary = await ActivityTracking.stopCurrentActivity();
```

## Activity Types

The plugin supports the following activity types:

- `WALKING`
- `RUNNING`
- `BIKING`

## Events

The plugin emits the following events:

- `STEP`: Updates the step count (walking and running only)
- `LOCATION`: Provides location updates with latitude, longitude, altitude, and speed
- `DISTANCE`: Reports the total distance traveled in kilometers
- `PAUSE`, `RESUME`, `STOP`: Activity state changes

## Notification

The plugin creates a foreground service with a notification that:

- Shows the current activity type
- Displays steps and distance information
- Provides pause/resume and stop action buttons
