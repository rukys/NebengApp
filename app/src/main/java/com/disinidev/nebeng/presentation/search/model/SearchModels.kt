package com.disinidev.nebeng.presentation.search.model

import com.disinidev.nebeng.domain.model.VehicleType

data class FavoriteLocation(
    val id: String,
    val iconEmoji: String,
    val title: String,
    val address: String
)

data class SearchHistoryItem(
    val id: String,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val optionsSummary: String
)

enum class VehicleFilter(val label: String) {
    ALL("Semua"),
    CAR("Mobil"),
    MOTORCYCLE("Motor"),
    FASTEST("Tercepat")
}

enum class SortBy(val label: String) {
    FASTEST("Tercepat"),
    CLOSEST_TIME("Jam Terdekat"),
    HIGHEST_RATING("Rating Tertinggi"),
    MOST_SEATS("Kursi Terbanyak")
}

enum class DepartureTimeSlot(val label: String) {
    ALL("Semua Jam"),
    MORNING("Pagi (06:00 - 09:00)"),
    AFTERNOON("Siang (11:00 - 14:00)"),
    EVENING("Sore (16:00 - 20:00)")
}

enum class DriverGender(val label: String) {
    ALL("Semua"),
    FEMALE("Wanita Saja 👩"),
    MALE("Pria 👨")
}

data class SearchFilterOptions(
    val sortBy: SortBy = SortBy.FASTEST,
    val vehicleType: VehicleType? = null, // null means all
    val departureTimeSlot: DepartureTimeSlot = DepartureTimeSlot.MORNING,
    val driverGender: DriverGender = DriverGender.ALL,
    val requireAc: Boolean = true,
    val requireNonSmoking: Boolean = true,
    val requireVerifiedOffice: Boolean = false,
    val minRating: Double? = 4.8
)

data class RideItemUi(
    val id: String,
    val driverName: String,
    val driverGender: DriverGender = DriverGender.MALE,
    val vehicleModel: String,
    val vehicleType: VehicleType,
    val departureTimeFormatted: String,
    val arrivalTimeFormatted: String,
    val availableSeats: Int,
    val availableSeatsText: String,
    val facilities: List<String>,
    val driverRating: Double = 4.9,
    val totalTrips: Int = 120,
    val isOfficeVerified: Boolean = true
)
