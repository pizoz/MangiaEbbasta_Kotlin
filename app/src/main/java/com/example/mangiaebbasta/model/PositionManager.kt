package com.example.mangiaebbasta.model

import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

class PositionManager(private val context: Context) {

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val geocoder = Geocoder(context)

    fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    suspend fun getLocation(): Location? {
        var location: Location? = null
        if (checkLocationPermission()) {
            val task: Task<Location> = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            )
            try {
                location = task.await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return location
    }

    fun getAddress(location: Location): String {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val address = addresses?.get(0)?.getAddressLine(0)
        if (address != null) {
            val array = address.split(",")
            val finalAddress = array[0] + "" + array[1]
            return finalAddress
        }
        return "No address found"

    }
}