package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.presentation.search.model.RideItemUi

data class CreateRideRequest(
    val driverId: String,
    val vehicleBrand: String,
    val vehicleModel: String,
    val vehiclePlate: String,
    val vehicleType: String, // 'car' or 'motorcycle'
    val maxPassengers: Int,
    val availableSeats: Int,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val departureTime: String,
    val notes: String? = null
)

interface RideRepository {
    suspend fun searchNearbyRides(
        lat: Double,
        lng: Double,
        radiusMeters: Int = 10000,
        vehicleType: String? = null,
        departureDate: String? = null
    ): Result<List<RideItemUi>>

    suspend fun createRide(request: CreateRideRequest): Result<String>
}
