package com.disinidev.nebeng.domain.repository

data class BookingResult(
    val bookingId: String,
    val pickupPin: String,
    val driverName: String = "Andi Pratama",
    val vehicleModel: String = "Avanza Silver",
    val vehiclePlate: String = "B 1234 ABC",
    val seatPosition: String = "front_left"
)

data class DriverBookingRequest(
    val bookingId: String,
    val rideId: String,
    val passengerId: String,
    val passengerName: String = "Budi Santoso",
    val pickupAddress: String = "Lawson Tebet Barat",
    val dropoffAddress: String = "Sudirman SCBD",
    val seatPosition: String = "Depan Kiri",
    val pickupPin: String = "489 201",
    val status: String = "pending",
    val notes: String? = null
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
}
