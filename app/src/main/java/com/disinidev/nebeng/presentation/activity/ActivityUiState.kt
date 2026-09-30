package com.disinidev.nebeng.presentation.activity

enum class ActivityFilter(val label: String) {
    ONGOING("Berjalan"),
    COMPLETED("Selesai"),
    CANCELED("Dibatalkan")
}

data class ActiveTrip(
    val bookingId: String,
    val statusText: String,
    val rawStatus: String = "confirmed",
    val pin: String,
    val driverName: String,
    val vehicleModel: String,
    val licensePlate: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val vehicleType: String = "car",
    val isDriver: Boolean = false
)

data class TripHistoryItem(
    val id: String,
    val origin: String,
    val destination: String,
    val timeText: String,
    val vehicleType: String = "Mobil",
    val driverName: String = "",
    val status: String = "SELESAI",
    val isDriver: Boolean = false
)

data class ActivityUiState(
    val selectedFilter: ActivityFilter = ActivityFilter.ONGOING,
    val activeTrip: ActiveTrip? = null,
    val completedTrips: List<TripHistoryItem> = emptyList(),
    val canceledTrips: List<TripHistoryItem> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val isCancelling: Boolean = false,
    val isActionInProgress: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

