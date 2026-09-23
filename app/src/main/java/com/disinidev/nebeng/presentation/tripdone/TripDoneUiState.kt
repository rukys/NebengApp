package com.disinidev.nebeng.presentation.tripdone

data class TripDoneUiState(
    val bookingId: String = "booking-001",
    val title: String = "Sampai di Tujuan!",
    val driverName: String = "Andi Pratama",
    val vehicleModel: String = "Avanza",
    val licensePlate: String = "B 1234 ABC",
    val rating: Int = 5,
    val reviewText: String = "",
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)
