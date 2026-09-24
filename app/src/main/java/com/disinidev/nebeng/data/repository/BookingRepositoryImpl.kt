package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.domain.repository.DriverBookingRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : BookingRepository {

    private val localBookings = ConcurrentHashMap<String, BookingResult>()
    private val localRequests = ConcurrentHashMap<String, DriverBookingRequest>().apply {
        put(
            "req-001",
            DriverBookingRequest(
                bookingId = "req-001",
                rideId = "ride-current",
                passengerId = "user-budi",
                passengerName = "Budi Santoso",
                pickupAddress = "Samping Lawson Tebet Barat",
                dropoffAddress = "Sudirman SCBD (Gedung Pasific)",
                seatPosition = "Depan Kiri",
                pickupPin = "489 201",
                status = "pending",
                notes = "Tolong tunggu di dekat minimarket ya mas"
            )
        )
    }

    override suspend fun bookSeat(
        rideId: String,
        passengerId: String,
        seatPosition: String
    ): Result<BookingResult> = withContext(Dispatchers.IO) {
        runCatching {
            var bookingId: String? = null
            var generatedPin = String.format("%03d %03d", Random.nextInt(100, 999), Random.nextInt(100, 999))

            // Attempt Supabase RPC book_seat if rideId is a valid UUID
            if (runCatching { UUID.fromString(rideId) }.isSuccess) {
                try {
                    val params = buildJsonObject {
                        put("p_ride_id", rideId)
                        put("p_passenger_id", passengerId)
                        put("p_seat_position", seatPosition)
                    }
                    val result = supabaseClient.postgrest.rpc(
                        function = "book_seat",
                        parameters = params
                    ).decodeAs<String>()
                    bookingId = result
                } catch (_: Exception) {
                    // Fall back to local generated booking
                }
            }

            val finalBookingId = bookingId ?: "booking_${UUID.randomUUID().toString().take(8)}"
            val isMotor = rideId.contains("ride_2") || seatPosition == "pillion"

            val result = BookingResult(
                bookingId = finalBookingId,
                pickupPin = generatedPin,
                driverName = if (isMotor) "Reza Hendra" else "Andi Pratama",
                vehicleModel = if (isMotor) "Yamaha NMAX Hitam" else "Toyota Avanza Silver",
                vehiclePlate = if (isMotor) "B 5678 XYZ" else "B 1234 ABC",
                seatPosition = seatPosition
            )

            localBookings[finalBookingId] = result
            result
        }
    }

    override suspend fun getBookingById(bookingId: String): Result<BookingResult> = withContext(Dispatchers.IO) {
        runCatching {
            localBookings[bookingId] ?: run {
                val isMotor = bookingId.contains("ride_2")
                BookingResult(
                    bookingId = bookingId,
                    pickupPin = if (isMotor) "215 889" else "489 201",
                    driverName = if (isMotor) "Reza Hendra" else "Andi Pratama",
                    vehicleModel = if (isMotor) "Yamaha NMAX Hitam" else "Toyota Avanza Silver",
                    vehiclePlate = if (isMotor) "B 5678 XYZ" else "B 1234 ABC",
                    seatPosition = if (isMotor) "pillion" else "front_left"
                )
            }
        }
    }

    override suspend fun rateTrip(
        bookingId: String,
        rating: Int,
        review: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (runCatching { UUID.fromString(bookingId) }.isSuccess) {
                try {
                    val updateData = mutableMapOf<String, Any>(
                        "driver_rating" to rating,
                        "status" to "done"
                    )
                    review?.takeIf { it.isNotBlank() }?.let { updateData["passenger_review"] = it }
                    supabaseClient.postgrest.from("bookings").update(updateData) {
                        filter {
                            eq("id", bookingId)
                        }
                    }
                } catch (_: Exception) {
                    // Safe offline fallback
                }
            }
            Unit
        }
    }

    override suspend fun getPendingRequests(driverId: String): Result<List<DriverBookingRequest>> = withContext(Dispatchers.IO) {
        runCatching {
            localRequests.values.filter { it.status == "pending" }.toList()
        }
    }

    override suspend fun respondBookingRequest(bookingId: String, accept: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val newStatus = if (accept) "confirmed" else "cancelled"
            localRequests[bookingId]?.let {
                localRequests[bookingId] = it.copy(status = newStatus)
            }
            if (runCatching { UUID.fromString(bookingId) }.isSuccess) {
                try {
                    supabaseClient.postgrest.from("bookings").update(
                        mapOf("status" to newStatus)
                    ) {
                        filter {
                            eq("id", bookingId)
                        }
                    }
                } catch (_: Exception) {}
            }
            Unit
        }
    }
}
