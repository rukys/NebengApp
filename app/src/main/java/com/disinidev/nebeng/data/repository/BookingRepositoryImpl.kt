package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.domain.repository.BookingActivityItem
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.domain.repository.DriverBookingRequest
import com.disinidev.nebeng.domain.repository.UserActivities
import com.disinidev.nebeng.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import timber.log.Timber

@Serializable
private data class RemotePendingBookingDto(
    val id: String,
    val seat_position: String,
    val pickup_pin: String,
    val status: String,
    val created_at: String? = null,
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
    val driver_id: String,
    val vehicle_brand: String? = null,
    val vehicle_model: String? = null,
    val vehicle_plate: String? = null,
    val vehicle_type: String? = null,
    val departure_time: String? = null,
    val users: RemoteBookingUserDto? = null
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
    val driver_id: String? = null,
    val vehicle_model: String? = null,
    val vehicle_plate: String? = null,
    val vehicle_type: String? = null,
    val pickup_address: String? = null,
    val dropoff_address: String? = null,
    val pickup_latitude: Double? = null,
    val pickup_longitude: Double? = null,
    val dropoff_latitude: Double? = null,
    val dropoff_longitude: Double? = null,
    val users: RemoteDetailUserDto? = null
)

@Serializable
private data class RemoteDetailUserDto(
    val id: String? = null,
    val full_name: String? = null
)

@Serializable
private data class RemotePinCheckDto(
    val id: String,
    val pickup_pin: String,
    val status: String
)

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository
) : BookingRepository {

    private val localBookings = ConcurrentHashMap<String, BookingResult>()
    private val localRequests = ConcurrentHashMap<String, DriverBookingRequest>()

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
            var generatedPin = String.format(Locale.US, "%03d %03d", Random.nextInt(100, 999), Random.nextInt(100, 999))

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
                Timber.e(e, "RPC book_seat failed: ${e.message}")
                // Fallback to direct row insert if RPC encounters concurrency or policy restrictions
                try {
                    val fallbackBookingId = UUID.randomUUID().toString()
                    val pin = String.format(Locale.US, "%06d", Random.nextInt(100000, 999999))
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
                    Timber.e(e2, "Direct insert fallback failed: ${e2.message}")
                }
            }

            val finalBookingId = bookingId ?: UUID.randomUUID().toString()

            val rideInfo = try {
                supabaseClient.postgrest.from("rides").select(
                    columns = Columns.raw(
                        "driver_id, vehicle_model, vehicle_plate, vehicle_type, pickup_address, dropoff_address, pickup_latitude, pickup_longitude, dropoff_latitude, dropoff_longitude, " +
                        "users!rides_driver_id_fkey(id, full_name)"
                    )
                ) {
                    filter {
                        eq("id", targetRideId)
                    }
                }.decodeSingleOrNull<RemoteDetailRideDto>()
            } catch (e: Exception) {
                null
            }

            val isMotor = rideInfo?.vehicle_type == "motorcycle" || seatPosition == "pillion"
            val driverId = rideInfo?.driver_id ?: rideInfo?.users?.id ?: ""
            val driverName = rideInfo?.users?.full_name ?: if (isMotor) "Pengemudi Motor" else "Pengemudi Mobil"
            val vehicleModel = rideInfo?.vehicle_model ?: if (isMotor) "Motor" else "Mobil"
            val vehiclePlate = rideInfo?.vehicle_plate ?: "-"

            val result = BookingResult(
                bookingId = finalBookingId,
                pickupPin = generatedPin,
                driverId = driverId,
                driverName = driverName,
                vehicleModel = vehicleModel,
                vehiclePlate = vehiclePlate,
                seatPosition = seatPosition,
                pickupAddress = rideInfo?.pickup_address ?: "",
                dropoffAddress = rideInfo?.dropoff_address ?: "",
                vehicleType = rideInfo?.vehicle_type ?: if (isMotor) "motorcycle" else "car",
                pickupLat = rideInfo?.pickup_latitude,
                pickupLng = rideInfo?.pickup_longitude,
                dropoffLat = rideInfo?.dropoff_latitude,
                dropoffLng = rideInfo?.dropoff_longitude
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
                            "rides(driver_id, vehicle_model, vehicle_plate, vehicle_type, pickup_address, dropoff_address, pickup_latitude, pickup_longitude, dropoff_latitude, dropoff_longitude, " +
                            "users!rides_driver_id_fkey(id, full_name))"
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
                            driverId = remote.rides?.driver_id ?: remote.rides?.users?.id ?: "",
                            driverName = remote.rides?.users?.full_name ?: "",
                            vehicleModel = remote.rides?.vehicle_model ?: "",
                            vehiclePlate = remote.rides?.vehicle_plate ?: "",
                            seatPosition = remote.seat_position,
                            pickupAddress = remote.rides?.pickup_address ?: "",
                            dropoffAddress = remote.rides?.dropoff_address ?: "",
                            vehicleType = remote.rides?.vehicle_type ?: "car",
                            status = remote.status,
                            pickupLat = remote.rides?.pickup_latitude,
                            pickupLng = remote.rides?.pickup_longitude,
                            dropoffLat = remote.rides?.dropoff_latitude,
                            dropoffLng = remote.rides?.dropoff_longitude
                        )
                        localBookings[bookingId] = result
                        return@runCatching result
                    }
                } catch (e: Exception) {
                    Timber.e(e, "getBookingById Supabase error: ${e.message}")
                }
            }

            localBookings[bookingId] ?: BookingResult(
                bookingId = bookingId,
                pickupPin = "",
                driverName = "",
                vehicleModel = "",
                vehiclePlate = "",
                seatPosition = ""
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
                        "status" to "completed"
                    )
                    review?.takeIf { it.isNotBlank() }?.let { updateData["passenger_review"] = it }
                    supabaseClient.postgrest.from("bookings").update(updateData) {
                        filter {
                            eq("id", bookingId)
                        }
                    }
                    localBookings[bookingId]?.let {
                        localBookings[bookingId] = it.copy(status = "completed")
                    }

                    // Also update driver rating in users table if found
                    try {
                        val bookingDetails = getBookingById(bookingId).getOrNull()
                        val targetDriverId = bookingDetails?.driverId
                        if (!targetDriverId.isNullOrBlank()) {
                            try {
                                supabaseClient.postgrest["users"].update(
                                    mapOf("average_rating" to rating.toDouble())
                                ) {
                                    filter { eq("id", targetDriverId) }
                                }
                            } catch (_: Exception) {
                                try {
                                    supabaseClient.postgrest["users"].update(
                                        mapOf("rating" to rating.toDouble())
                                    ) {
                                        filter { eq("id", targetDriverId) }
                                    }
                                } catch (_: Exception) {}
                            }
                        } else {
                            val driverName = bookingDetails?.driverName
                            if (!driverName.isNullOrBlank()) {
                                val drivers = supabaseClient.postgrest["users"].select {
                                    filter { eq("full_name", driverName) }
                                    limit(1)
                                }.decodeList<RemoteBookingUserDto>()
                                val driver = drivers.firstOrNull()
                                if (driver != null) {
                                    supabaseClient.postgrest["users"].update(
                                        mapOf("average_rating" to rating.toDouble())
                                    ) {
                                        filter { eq("id", driver.id) }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error updating driver profile rating: ${e.message}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "rateTrip Supabase error: ${e.message}")
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

                return@runCatching list.map { it.toDriverBookingRequest() }
            } catch (e: Exception) {
                Timber.e(e, "getPendingRequests Supabase error: ${e.message}")
                localRequests.values.filter { it.status == "pending" }.toList()
            }
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
                    Timber.e(e, "respondBookingRequest Supabase error: ${e.message}")
                }
            }
            Unit
        }
    }

    override suspend fun cancelBooking(
        bookingId: String,
        reason: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            localBookings.remove(bookingId)
            localRequests[bookingId]?.let {
                localRequests[bookingId] = it.copy(status = "cancelled")
            }
            if (runCatching { UUID.fromString(bookingId) }.isSuccess) {
                try {
                    val payload = mutableMapOf<String, Any>("status" to "cancelled")
                    if (!reason.isNullOrBlank()) {
                        payload["cancellation_reason"] = reason
                    }
                    supabaseClient.postgrest.from("bookings").update(payload) {
                        filter {
                            eq("id", bookingId)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "cancelBooking Supabase error: ${e.message}")
                    // Fallback to updating status only if cancellation_reason column is not in schema
                    try {
                        supabaseClient.postgrest.from("bookings").update(
                            mapOf("status" to "cancelled")
                        ) {
                            filter {
                                eq("id", bookingId)
                            }
                        }
                    } catch (fallbackEx: Exception) {
                        Timber.e(fallbackEx, "cancelBooking fallback error: ${fallbackEx.message}")
                    }
                }
            }
            Unit
        }
    }

    override suspend fun verifyPickupPin(bookingId: String, pin: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            localBookings[bookingId]?.let {
                if (it.pickupPin.trim() != pin.trim()) {
                    throw IllegalArgumentException("PIN penjemputan salah. Periksa kembali PIN penumpang.")
                }
                localBookings[bookingId] = it.copy(status = "picked_up")
            }
            if (runCatching { UUID.fromString(bookingId) }.isSuccess) {
                val remote = try {
                    supabaseClient.postgrest.from("bookings").select(
                        columns = Columns.raw("id, pickup_pin, status")
                    ) {
                        filter { eq("id", bookingId) }
                    }.decodeSingleOrNull<RemotePinCheckDto>()
                } catch (e: Exception) {
                    null
                }

                if (remote != null && remote.pickup_pin.trim() != pin.trim()) {
                    throw IllegalArgumentException("PIN penjemputan salah. Periksa kembali PIN penumpang.")
                }

                supabaseClient.postgrest.from("bookings").update(
                    mapOf("status" to "picked_up")
                ) {
                    filter { eq("id", bookingId) }
                }
            }
            Unit
        }
    }

    override suspend fun completeTrip(bookingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            localBookings[bookingId]?.let {
                localBookings[bookingId] = it.copy(status = "completed")
            }
            if (runCatching { UUID.fromString(bookingId) }.isSuccess) {
                supabaseClient.postgrest.from("bookings").update(
                    mapOf("status" to "completed")
                ) {
                    filter { eq("id", bookingId) }
                }
            }
            Unit
        }
    }

    override suspend fun getUserActivities(userUuid: String): Result<UserActivities> = withContext(Dispatchers.IO) {
        runCatching {
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.of("Asia/Jakarta"))

            // 1. Fetch user bookings as passenger
            val passengerBookings = try {
                supabaseClient.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, created_at, " +
                        "rides(id, pickup_address, dropoff_address, vehicle_brand, vehicle_model, vehicle_plate, vehicle_type, departure_time, driver_id, " +
                        "users!rides_driver_id_fkey(id, full_name, avatar_url)), " +
                        "users!bookings_passenger_id_fkey(id, full_name, avatar_url)"
                    )
                ) {
                    filter {
                        eq("passenger_id", userUuid)
                    }
                    order("created_at", Order.DESCENDING)
                }.decodeList<RemotePendingBookingDto>()
            } catch (_: Exception) {
                emptyList()
            }

            // 2. Fetch driver bookings
            val driverBookings = try {
                supabaseClient.from("bookings").select(
                    columns = Columns.raw(
                        "id, seat_position, pickup_pin, status, created_at, " +
                        "rides!inner(id, pickup_address, dropoff_address, vehicle_brand, vehicle_model, vehicle_plate, vehicle_type, departure_time, driver_id), " +
                        "users!bookings_passenger_id_fkey(id, full_name, avatar_url)"
                    )
                ) {
                    filter {
                        eq("rides.driver_id", userUuid)
                    }
                    order("created_at", Order.DESCENDING)
                }.decodeList<RemotePendingBookingDto>()
            } catch (_: Exception) {
                emptyList()
            }

            val allBookings = passengerBookings + driverBookings
            if (allBookings.isNotEmpty()) {
                val ongoing = allBookings.firstOrNull { it.status in listOf("pending", "confirmed", "picked_up") }
                val completed = allBookings.filter { it.status in listOf("done", "completed") }
                val cancelled = allBookings.filter { it.status == "cancelled" }

                val activeItem = ongoing?.let { b ->
                    val isDriver = b.rides?.driver_id == userUuid
                    val counterpartName = if (isDriver) b.users?.full_name ?: "Penumpang" else b.rides?.users?.full_name ?: "Pengemudi"
                    val formattedTime = runCatching {
                        b.rides?.departure_time?.let { dateFormatter.format(Instant.parse(it)) }
                            ?: b.created_at?.let { dateFormatter.format(Instant.parse(it)) }
                    }.getOrNull() ?: "Segera"

                    BookingActivityItem(
                        id = b.id,
                        origin = b.rides?.pickup_address ?: "-",
                        destination = b.rides?.dropoff_address ?: "-",
                        timeText = formattedTime,
                        vehicleType = if (b.rides?.vehicle_type == "motorcycle") "Motor" else "Mobil",
                        counterpartName = counterpartName,
                        status = b.status,
                        pin = b.pickup_pin,
                        vehicleModel = b.rides?.vehicle_model ?: "-",
                        licensePlate = b.rides?.vehicle_plate ?: "-",
                        isDriver = isDriver
                    )
                }

                val completedItems = completed.map { b ->
                    val isDriver = b.rides?.driver_id == userUuid
                    val counterpartName = if (isDriver) b.users?.full_name ?: "Penumpang" else b.rides?.users?.full_name ?: "Pengemudi"
                    val formattedTime = runCatching {
                        b.created_at?.let { dateFormatter.format(Instant.parse(it)) }
                    }.getOrNull() ?: "Selesai"
                    BookingActivityItem(
                        id = b.id,
                        origin = b.rides?.pickup_address ?: "-",
                        destination = b.rides?.dropoff_address ?: "-",
                        timeText = formattedTime,
                        vehicleType = if (b.rides?.vehicle_type == "motorcycle") "Motor" else "Mobil",
                        counterpartName = counterpartName,
                        status = "SELESAI",
                        vehicleModel = b.rides?.vehicle_model ?: "",
                        licensePlate = b.rides?.vehicle_plate ?: "",
                        isDriver = isDriver
                    )
                }

                val canceledItems = cancelled.map { b ->
                    val isDriver = b.rides?.driver_id == userUuid
                    val counterpartName = if (isDriver) b.users?.full_name ?: "Penumpang" else b.rides?.users?.full_name ?: "Pengemudi"
                    val formattedTime = runCatching {
                        b.created_at?.let { dateFormatter.format(Instant.parse(it)) }
                    }.getOrNull() ?: "Dibatalkan"
                    BookingActivityItem(
                        id = b.id,
                        origin = b.rides?.pickup_address ?: "-",
                        destination = b.rides?.dropoff_address ?: "-",
                        timeText = formattedTime,
                        vehicleType = if (b.rides?.vehicle_type == "motorcycle") "Motor" else "Mobil",
                        counterpartName = counterpartName,
                        status = "DIBATALKAN",
                        vehicleModel = b.rides?.vehicle_model ?: "",
                        licensePlate = b.rides?.vehicle_plate ?: "",
                        isDriver = isDriver
                    )
                }

                UserActivities(
                    activeTrip = activeItem,
                    completedTrips = completedItems,
                    canceledTrips = canceledItems
                )
            } else {
                UserActivities(
                    activeTrip = null,
                    completedTrips = emptyList(),
                    canceledTrips = emptyList()
                )
            }
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
                        "full_name" to "Mitra Pengemudi",
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
            Timber.e(e, "ensureDemoRideInSupabase error: ${e.message}")
            return deterministicUuid
        }
    }
}
