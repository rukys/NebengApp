package com.disinidev.nebeng.presentation.tracking

import com.disinidev.nebeng.domain.model.VehicleType

enum class TripStage {
    WAITING_FOR_DRIVER,   // Menunggu konfirmasi pengemudi
    PICKUP_EN_ROUTE,      // Fase 1: Driver menuju titik penjemputan
    PICKUP_ARRIVED,       // Fase 1: Driver tiba di titik jemput, verifikasi PIN
    IN_TRANSIT,           // Fase 2: Dalam perjalanan menuju destinasi
    ARRIVED_DESTINATION   // Fase 3: Tiba di tujuan akhir
}

data class LiveTrackingUiState(
    val bookingId: String = "",
    val driverName: String = "",
    val vehicleModel: String = "",
    val vehiclePlate: String = "",
    val vehicleType: VehicleType = VehicleType.CAR,
    val etaMinutes: Int = 0,
    val distanceMeters: Int = 0,
    val pickupLocation: String = "",
    val destinationLocation: String = "",
    val bookingPin: String = "",
    val statusText: String = "Driver Sedang Menjemput",
    val isEmergencyDialogOpen: Boolean = false,
    val progress: Float = 0.0f,
    val driverBearing: Float = 45f,
    val driverCurrentLat: Double = -6.2245,
    val driverCurrentLng: Double = 106.8048,
    val pickupLat: Double = -6.2215,
    val pickupLng: Double = 106.8065,
    val destinationLat: Double = -6.2180,
    val destinationLng: Double = 106.8105,
    val isArrived: Boolean = false,
    val tripStage: TripStage = TripStage.PICKUP_EN_ROUTE,
    val currentSpeedKmh: Int = 0,
    val isCurrentUserDriver: Boolean = false
)
