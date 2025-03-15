package de.buseslaar.tracking.activity_tracking.sensor

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit

class LocationSensor {

    var locationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    constructor(context: Context, onLocationUpdatedListener: (locations: List<Location>) -> Unit) {
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                onLocationUpdatedListener(result.locations)
            }
        }

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        // Check permission
        if (locationCallback != null && locationClient != null) {
            locationClient!!.requestLocationUpdates(
                LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    TimeUnit.SECONDS.toMillis(3)
                ).build(), locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let { locationClient?.removeLocationUpdates(it) }
    }

}