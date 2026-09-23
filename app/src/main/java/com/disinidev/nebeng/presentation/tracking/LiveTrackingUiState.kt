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
    val isEmergencyDialogOpen: Boolean = false
)
