package com.disinidev.nebeng.data.repository

import android.util.Log
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.domain.repository.DriverBookingRequest
import com.disinidev.nebeng.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Serializable
private data class RemotePendingBookingDto(
    val id: String,
    val seat_position: String,
    val pickup_pin: String,
    val status: String,
    val rides: RemoteBookingRideDto? = null,
    val users: RemoteBookingUserDto? = null
) {
    fun toDriverBookingRequest(): DriverBookingRequest {
        return DriverBookingRequest(
            bookingId = id,
            rideId = rides?.id ?: "",
            passengerId = users?.id ?: "",
            passengerName = users?.full_name ?: "Penumpang Nebeng",
            pickupAddress = rides?.pickup_address ?: "Titik Jemput",
            dropoffAddress = rides?.dropoff_address ?: "Titik Tujuan",
            seatPosition = seat_position,
            pickupPin = pickup_pin,
            status = status,
            notes = "Tolong konfirmasi penjemputan"
        )
    }
}

@Serializable
private data class RemoteBookingRideDto(
    val id: String,
    val pickup_address: String,
    val dropoff_address: String,
    val driver_id: String
)

@Serializable
private data class RemoteBookingUserDto(
    val id: String,
    val full_name: String? = null,
    val phone_number: String? = null,
    val avatar_url: String? = null
)

@Serializable
private data class RemoteBookingDetailDto(
    val id: String,
    val pickup_pin: String,
    val seat_position: String,
    val status: String,
    val rides: RemoteDetailRideDto? = null
)

@Serializable
private data class RemoteDetailRideDto(
    val vehicle_model: String,
    val vehicle_plate: String,
    val users: RemoteDetailUserDto? = null
)

@Serializable
private data class RemoteDetailUserDto(
    val full_name: String? = null
)

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository
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

    private fun mapSeatPositionToDb(seat: String): String {
        return when (seat.lowercase()) {
            "depan_kiri", "front_left" -> "front_left"
            "tengah_kiri", "belakang_kiri", "rear_left" -> "rear_left"
            "tengah_tengah", "belakang_tengah", "rear_center" -> "rear_center"
            "tengah_kanan", "belakang_kanan", "rear_right" -> "rear_right"
            "pillion", "bonceng", "boncengan" -> "pillion"
            else -> "rear_left"
        }
    }

    override suspend fun bookSeat(
        rideId: String,
        passengerId: String,
        seatPosition: String
    ): Result<BookingResult> = withContext(Dispatchers.IO) {
        runCatching {
            val dbSeatPosition = mapSeatPositionToDb(seatPosition)
            var targetRideId = rideId
            var bookingId: String? = null
            var generatedPin = String.format("%03d %03d", Random.nextInt(100, 999), Random.nextInt(100, 999))

            // 1. If rideId is a mock string (e.g. "ride_1"), ensure a real ride row exists in Supabase
            if (!runCatching { UUID.fromString(targetRideId) }.isSuccess) {
                targetRideId = ensureDemoRideInSupabase(rideId)
            }

            // 2. Call Supabase RPC book_seat
            try {
                val params = buildJsonObject {
                    put("p_ride_id", targetRideId)
                    put("p_passenger_id", passengerId)
                    put("p_seat_position", dbSeatPosition)
                }
                val result = supabaseClient.postgrest.rpc(
                    function = "book_seat",
                    parameters = params
                ).decodeAs<String>()
                bookingId = result
            } catch (e: Exception) {
                Log.e("BookingRepository", "RPC book_seat failed: ${e.message}", e)
                // Fallback to direct row insert if RPC encounters concurrency or policy restrictions
                try {
                    val fallbackBookingId = UUID.randomUUID().toString()
                    val pin = String.format("%06d", Random.nextInt(100000, 999999))
                    val insertPayload = mapOf(
                        "id" to fallbackBookingId,
                        "ride_id" to targetRideId,
                        "passenger_id" to passengerId,
                        "seat_position" to dbSeatPosition,
                        "pickup_pin" to pin,
                        "status" to "pending"
                    )
                    supabaseClient.postgrest.from("bookings").insert(insertPayload)
                    bookingId = fallbackBookingId
                    generatedPin = "${pin.take(3)} ${pin.takeLast(3)}"
                } catch (e2: Exception) {
                    Log.e("BookingRepository", "Direct insert fallback failed: ${e2.message}", e2)
                }
            }

            val finalBookingId = bookingId ?: UUID.randomUUID().toString()
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
            val cached = localBookings[bookingId]
            if (cached != null) return@runCatching cached

            if (runCatching { UUID.fromString(bookingId) }.isSuccess) {
                try {
                    val remote = supabaseClient.postgrest.from("bookings").select(
                        columns = Columns.raw(
                            "id, pickup_pin, seat_position, status, " +
                            "rides(vehicle_model, vehicle_plate, " +
                            "users!rides_driver_id_fkey(full_name))"
                        )
                    ) {
                        filter {
                            eq("id", bookingId)
                        }
                    }.decodeSingleOrNull<RemoteBookingDetailDto>()

                    if (remote != null) {
                        val result = BookingResult(
                            bookingId = remote.id,
                            pickupPin = remote.pickup_pin,
                            driverName = remote.rides?.users?.full_name ?: "Andi Pratama",
                            vehicleModel = remote.rides?.vehicle_model ?: "Toyota Avanza Silver",
                            vehiclePlate = remote.rides?.vehicle_plate ?: "B 1234 ABC",
                            seatPosition = remote.seat_position
                        )
                        localBookings[bookingId] = result
                        return@runCatching result
                    }
                } catch (e: Exception) {
                    Log.e("BookingRepository", "getBookingById Supabase error: ${e.message}", e)
                }
            }

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
                } catch (e: Exception) {
                    Log.e("BookingRepository", "rateTrip Supabase error: ${e.message}", e)
                }
            }
            Unit
        }
    }

    override suspend fun getPendingRequests(driverId: String): Result<List<DriverBookingRequest>> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val driverUuid = if (runCatching { UUID.fromString(driverId) }.isSuccess) {
                    driverId
                } else {
                    userRepository.getCurrentUserUuid()
                }

                val list = supabaseClient.postgrest.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, " +
                        "rides!inner(id, pickup_address, dropoff_address, driver_id), " +
                        "users!bookings_passenger_id_fkey(id, full_name, phone_number, avatar_url)"
                    )
                ) {
                    filter {
                        eq("status", "pending")
                        eq("rides.driver_id", driverUuid)
                    }
                }.decodeList<RemotePendingBookingDto>()

                if (list.isNotEmpty()) {
                    val requests = list.map { it.toDriverBookingRequest() }
                    return@runCatching requests
                }
            } catch (e: Exception) {
                Log.e("BookingRepository", "getPendingRequests Supabase error: ${e.message}", e)
            }

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
                } catch (e: Exception) {
                    Log.e("BookingRepository", "respondBookingRequest Supabase error: ${e.message}", e)
                }
            }
            Unit
        }
    }

    private suspend fun ensureDemoRideInSupabase(rideKey: String): String {
        val deterministicUuid = UUID.nameUUIDFromBytes("nebeng_demo_ride_$rideKey".toByteArray()).toString()
        try {
            val existing = supabaseClient.postgrest.from("rides").select {
                filter { eq("id", deterministicUuid) }
            }.decodeList<Map<String, String>>()
            if (existing.isNotEmpty()) {
                return deterministicUuid
            }

            val driverUuid = UUID.nameUUIDFromBytes("nebeng_demo_driver".toByteArray()).toString()
            runCatching {
                supabaseClient.postgrest.from("users").upsert(
                    mapOf(
                        "id" to driverUuid,
                        "firebase_uid" to "demo_driver_andi",
                        "full_name" to "Andi Pratama",
                        "phone_number" to "081299887766",
                        "role" to "driver"
                    )
                ) { onConflict = "id" }
            }

            val isMotor = rideKey.contains("ride_2")
            val ridePayload = mapOf(
                "id" to deterministicUuid,
                "driver_id" to driverUuid,
                "vehicle_brand" to if (isMotor) "Yamaha" else "Toyota",
                "vehicle_model" to if (isMotor) "Yamaha NMAX Hitam" else "Toyota Avanza Silver",
                "vehicle_plate" to if (isMotor) "B 5678 XYZ" else "B 1234 ABC",
                "vehicle_type" to if (isMotor) "motorcycle" else "car",
                "max_passengers" to if (isMotor) 1 else 3,
                "available_seats" to if (isMotor) 1 else 3,
                "pickup_address" to "Stasiun Tebet (Pintu Barat)",
                "pickup_location" to "POINT(106.8580 -6.2297)",
                "dropoff_address" to "SCBD Sudirman (Lot 8 & Pasific)",
                "dropoff_location" to "POINT(106.8105 -6.2180)",
                "departure_time" to Instant.now().plusSeconds(3600).toString(),
                "status" to "available",
                "notes" to "Nebeng bareng santai, 100% gratis"
            )
            supabaseClient.postgrest.from("rides").upsert(ridePayload) { onConflict = "id" }
            return deterministicUuid
        } catch (e: Exception) {
            Log.e("BookingRepository", "ensureDemoRideInSupabase error: ${e.message}", e)
            return deterministicUuid
        }
    }
}
