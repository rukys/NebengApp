package com.disinidev.nebeng.presentation.tracking

import com.disinidev.nebeng.domain.model.VehicleType

data class LiveTrackingUiState(
    val bookingId: String = "",
    val driverName: String = "Andi P.",
    val vehicleModel: String = "Avanza Silver",
    val vehiclePlate: String = "B 1234 ABC",
    val vehicleType: VehicleType = VehicleType.CAR,
    val etaMinutes: Int = 3,
    val distanceMeters: Int = 450,
    val pickupLocation: String = "Jemput: Pintu Barat Lawson",
    val destinationLocation: String = "SCBD Lot 8 (Tujuan)",
    val bookingPin: String = "489 201",
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
    val isArrived: Boolean = false
)
