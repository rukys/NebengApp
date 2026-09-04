package com.disinidev.nebeng.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class DefaultLocationClient @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : LocationClient {

    override fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        try {
            val location = fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await() ?: fusedLocationProviderClient.lastLocation.await()

            if (location != null) {
                val addressName = getAddressFromCoordinates(location.latitude, location.longitude)
                UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    addressName = addressName
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("id", "ID"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val address = addresses.firstOrNull()
                            val name = when {
                                address == null -> null
                                !address.subLocality.isNullOrBlank() && !address.locality.isNullOrBlank() ->
                                    "${address.subLocality}, ${address.locality}"
                                !address.locality.isNullOrBlank() -> address.locality
                                !address.subAdminArea.isNullOrBlank() -> address.subAdminArea
                                else -> address.thoroughfare ?: address.featureName
                            }
                            continuation.resume(name)
                        }
                    }
                } else {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address = addresses?.firstOrNull()
                    when {
                        address == null -> null
                        !address.subLocality.isNullOrBlank() && !address.locality.isNullOrBlank() ->
                            "${address.subLocality}, ${address.locality}"
                        !address.locality.isNullOrBlank() -> address.locality
                        !address.subAdminArea.isNullOrBlank() -> address.subAdminArea
                        else -> address.thoroughfare ?: address.featureName
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
}
