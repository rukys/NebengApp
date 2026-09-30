package com.disinidev.nebeng.domain.model

import java.time.Instant

data class Booking(
    val id: String,
    val rideId: String,
    val ride: Ride? = null,
    val passengerId: String,
    val passenger: User? = null,
    val seatPosition: SeatPosition,
    val pickupPin: String,
    val status: BookingStatus = BookingStatus.PENDING,
    val hasTipped: Boolean = false,
    val passengerRating: Int? = null,
    val driverRating: Int? = null,
    val passengerReview: String? = null,
    val driverReview: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    PICKED_UP,
    DONE,
    CANCELLED;

    companion object {
        fun fromString(value: String): BookingStatus = runCatching {
            valueOf(value.uppercase())
        }.getOrDefault(PENDING)
    }
}

enum class SeatPosition(val value: String, val label: String) {
    FRONT_LEFT("front_left", "Depan Kiri"),
    REAR_LEFT("rear_left", "Belakang Kiri"),
    REAR_CENTER("rear_center", "Belakang Tengah"),
    REAR_RIGHT("rear_right", "Belakang Kanan"),
    PILLION("pillion", "Boncengan");

    companion object {
        fun fromString(value: String): SeatPosition = entries.firstOrNull {
            it.value.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: REAR_LEFT
    }
}
