package com.disinidev.nebeng.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchNearbyRidesParams(
    @SerialName("user_lat") val userLat: Double,
    @SerialName("user_lng") val userLng: Double,
    @SerialName("radius_meters") val radiusMeters: Int = 10000,
    @SerialName("p_vehicle_type") val vehicleType: String? = null,
    @SerialName("departure_date") val departureDate: String? = null
)
