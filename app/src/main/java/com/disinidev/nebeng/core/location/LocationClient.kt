package com.disinidev.nebeng.core.location

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val addressName: String? = null
)

interface LocationClient {
    suspend fun getCurrentLocation(): UserLocation?
    fun hasLocationPermission(): Boolean
}
