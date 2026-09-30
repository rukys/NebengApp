package com.disinidev.nebeng.domain.model

import java.time.Instant

data class Ride(
    val id: String,
    val driverId: String,
    val driver: User? = null,
    val vehicleInfo: VehicleInfo,
    val maxPassengers: Int,
    val availableSeats: Int,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val routePolyline: String? = null,
    val departureTime: Instant,
    val status: RideStatus = RideStatus.AVAILABLE,
    val notes: String? = null,
    val createdAt: Instant = Instant.now()
)

enum class RideStatus {
    AVAILABLE,
    FULL,
    ONGOING,
    DONE,
    CANCELLED;

    companion object {
        fun fromString(value: String): RideStatus = runCatching {
            valueOf(value.uppercase())
        }.getOrDefault(AVAILABLE)
    }
}
