package com.example.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val area: String
)

class LocationService(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): UserLocation {
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            if (location != null) {
                getAddressFromLocation(location.latitude, location.longitude)
            } else {
                getDefaultLocation()
            }
        } catch (e: Exception) {
            getDefaultLocation()
        }
    }

    fun getAddressFromLocation(lat: Double, lon: Double): UserLocation {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0) ?: "Hirapur, Dhanbad, Jharkhand"
                val city = address.locality ?: "Dhanbad"
                val area = address.subLocality ?: "Hirapur"
                UserLocation(lat, lon, fullAddress, city, area)
            } else {
                getDefaultLocation(lat, lon)
            }
        } catch (e: Exception) {
            getDefaultLocation(lat, lon)
        }
    }

    fun getDefaultLocation(lat: Double = 23.8141, lon: Double = 86.4412): UserLocation {
        return UserLocation(
            latitude = lat,
            longitude = lon,
            address = "Court Road, Hirapur, Dhanbad, Jharkhand 826001",
            city = "Dhanbad",
            area = "Hirapur"
        )
    }
}
