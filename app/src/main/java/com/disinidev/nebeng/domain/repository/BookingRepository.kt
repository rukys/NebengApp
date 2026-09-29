package com.disinidev.nebeng.domain.repository

data class BookingResult(
    val bookingId: String,
    val pickupPin: String,
    val driverName: String = "",
    val vehicleModel: String = "",
    val vehiclePlate: String = "",
    val seatPosition: String = "",
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val vehicleType: String = "car"
)

data class DriverBookingRequest(
    val bookingId: String,
    val rideId: String,
    val passengerId: String,
    val passengerName: String = "",
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val seatPosition: String = "",
    val pickupPin: String = "",
    val status: String = "pending",
    val notes: String? = null
)

data class BookingActivityItem(
    val id: String,
    val origin: String,
    val destination: String,
    val timeText: String,
    val vehicleType: String,
    val counterpartName: String,
    val status: String,
    val pin: String = "",
    val vehicleModel: String = "",
    val licensePlate: String = ""
)

data class UserActivities(
    val activeTrip: BookingActivityItem? = null,
    val completedTrips: List<BookingActivityItem> = emptyList(),
    val canceledTrips: List<BookingActivityItem> = emptyList()
)

interface BookingRepository {
    suspend fun bookSeat(
        rideId: String,
        passengerId: String,
        seatPosition: String
    ): Result<BookingResult>

    suspend fun getBookingById(bookingId: String): Result<BookingResult>

    suspend fun rateTrip(
        bookingId: String,
        rating: Int,
        review: String?
    ): Result<Unit>

    suspend fun getPendingRequests(driverId: String): Result<List<DriverBookingRequest>>

    suspend fun respondBookingRequest(bookingId: String, accept: Boolean): Result<Unit>
 
    suspend fun cancelBooking(bookingId: String): Result<Unit>

    suspend fun getUserActivities(userUuid: String): Result<UserActivities>
}
