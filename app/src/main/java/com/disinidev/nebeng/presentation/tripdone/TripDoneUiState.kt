package com.disinidev.nebeng.presentation.tripdone

data class TripDoneUiState(
    val bookingId: String = "",
    val title: String = "Sampai di Tujuan!",
    val driverName: String = "",
    val vehicleModel: String = "",
    val licensePlate: String = "",
    val rating: Int = 5,
    val availableTags: List<String> = listOf(
        "Tepat Waktu",
        "Mobil Bersih & Wangi",
        "Mengemudi Aman",
        "Ramah & Sopan",
        "Musik Asik",
        "Rute Efisien"
    ),
    val selectedTags: Set<String> = emptySet(),
    val reviewText: String = "",
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)
