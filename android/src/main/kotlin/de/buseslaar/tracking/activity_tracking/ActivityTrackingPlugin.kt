package de.buseslaar.tracking.activity_tracking

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import de.buseslaar.tracking.activity_tracking.activitymanager.ActivityManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** ActivityTrackingPlugin */
class ActivityTrackingPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    private lateinit var activityManager: ActivityManager


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        var context = flutterPluginBinding.applicationContext
        this.context = context.applicationContext
        this.activityManager = ActivityManager(context.applicationContext)

        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "activity_tracking")
        channel.setMethodCallHandler(this)
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMethodCall(call: MethodCall, result: Result) {
        var activityManager = this.activityManager
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }

            "startActivity" -> {
                activityManager.startActivity(call.argument<String>("type").toString())
                result.success("Starting Activity")
            }

            "stopCurrentActivity" -> {
                var activity = activityManager.stopCurrentActivity()
                if (activity != null) {
                    // TODO: Improve data type mapping, because communication can only handle native types
                    result.success(activity.parseToJSON())
                } else {
                    result.error("Error", "Stopping Activity failed", null)
                }
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
