package com.disinidev.nebeng.presentation.activity

enum class ActivityFilter(val label: String) {
    ONGOING("Berjalan (1)"),
    COMPLETED("Selesai (14)"),
    CANCELED("Dibatalkan")
}

data class ActiveTrip(
    val bookingId: String = "booking-001",
    val statusText: String = "PENJEMPUTAN 5 MNT LAGI",
    val pin: String = "489 201",
    val driverName: String = "Andi Pratama",
    val vehicleModel: String = "Avanza",
    val licensePlate: String = "B 1234 ABC",
    val pickupAddress: String = "Stasiun Tebet",
    val dropoffAddress: String = "SCBD Sudirman",
    val vehicleType: String = "car"
)

data class TripHistoryItem(
    val id: String,
    val origin: String,
    val destination: String,
    val timeText: String,
    val vehicleType: String = "Mobil",
    val driverName: String = "",
    val status: String = "SELESAI"
)

data class ActivityUiState(
    val selectedFilter: ActivityFilter = ActivityFilter.ONGOING,
    val activeTrip: ActiveTrip? = ActiveTrip(),
    val completedTrips: List<TripHistoryItem> = listOf(
        TripHistoryItem(
            id = "trip-1",
            origin = "Pancoran",
            destination = "Kuningan",
            timeText = "Kemarin, 08:30",
            vehicleType = "Mobil",
            driverName = "Reza Hendra"
        ),
        TripHistoryItem(
            id = "trip-2",
            origin = "Tebet",
            destination = "Cilandak Barat",
            timeText = "30 Agu, 17:15",
            vehicleType = "Motor",
            driverName = "Dimas Setiawan"
        ),
        TripHistoryItem(
            id = "trip-3",
            origin = "Stasiun Cawang",
            destination = "SCBD Sudirman",
            timeText = "28 Agu, 07:45",
            vehicleType = "Mobil",
            driverName = "Andi Pratama"
        ),
        TripHistoryItem(
            id = "trip-4",
            origin = "Kuningan City",
            destination = "Pasar Minggu",
            timeText = "25 Agu, 18:00",
            vehicleType = "Mobil",
            driverName = "Bambang Pamungkas"
        )
    ),
    val canceledTrips: List<TripHistoryItem> = listOf(
        TripHistoryItem(
            id = "trip-cancel-1",
            origin = "Blok M",
            destination = "Tebet",
            timeText = "22 Agu, 08:15",
            vehicleType = "Motor",
            driverName = "Fajar Nugraha",
            status = "DIBATALKAN"
        )
    ),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false
)
