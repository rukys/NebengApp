package com.disinidev.nebeng.data.repository

import android.util.Log
import com.disinidev.nebeng.data.model.RideSearchResultDto
import com.disinidev.nebeng.domain.model.Ride
import com.disinidev.nebeng.domain.model.RideStatus
import com.disinidev.nebeng.domain.model.User
import com.disinidev.nebeng.domain.model.UserRole
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.CreateRideRequest
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.RideItemUi
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class RemoteRideDto(
    val id: String,
    val driver_id: String,
    val vehicle_brand: String,
    val vehicle_model: String,
    val vehicle_plate: String,
    val vehicle_type: String,
    val max_passengers: Int,
    val available_seats: Int,
    val pickup_address: String,
    val dropoff_address: String,
    val departure_time: String,
    val status: String,
    val notes: String? = null,
    val users: RemoteDriverDto? = null
)

@Serializable
private data class RemoteDriverDto(
    val id: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val average_rating: Float? = null,
    val total_trips: Int? = null
)

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : RideRepository {

    override suspend fun searchNearbyRides(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        vehicleType: String?,
        departureDate: String?
    ): Result<List<RideItemUi>> = withContext(Dispatchers.IO) {
        runCatching {
            val params = buildJsonObject {
                put("user_lat", lat)
                put("user_lng", lng)
                put("radius_meters", radiusMeters)
                vehicleType?.let { put("p_vehicle_type", it) }
                departureDate?.let { put("departure_date", it) }
            }
            val dtoList = supabaseClient.postgrest.rpc(
                function = "search_nearby_rides",
                parameters = params
            ).decodeList<RideSearchResultDto>()

            dtoList.map { it.toUiModel() }
        }
    }

    override suspend fun createRide(request: CreateRideRequest): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val newId = UUID.randomUUID().toString()
            try {
                val insertPayload = mapOf(
                    "id" to newId,
                    "driver_id" to request.driverId,
                    "vehicle_brand" to request.vehicleBrand,
                    "vehicle_model" to request.vehicleModel,
                    "vehicle_plate" to request.vehiclePlate,
                    "vehicle_type" to request.vehicleType,
                    "max_passengers" to request.maxPassengers,
                    "available_seats" to request.availableSeats,
                    "pickup_address" to request.pickupAddress,
                    "pickup_location" to "POINT(${request.pickupLng} ${request.pickupLat})",
                    "dropoff_address" to request.dropoffAddress,
                    "dropoff_location" to "POINT(${request.dropoffLng} ${request.dropoffLat})",
                    "departure_time" to request.departureTime,
                    "status" to "available",
                    "notes" to (request.notes ?: "")
                )
                supabaseClient.postgrest.from("rides").insert(insertPayload)
            } catch (e: Exception) {
                Log.e("RideRepository", "Supabase createRide error: ${e.message}", e)
            }
            newId
        }
    }

    override suspend fun getPopularRides(): Result<List<Ride>> = withContext(Dispatchers.IO) {
        runCatching {
            val remoteList = supabaseClient.postgrest["rides"].select(
                columns = Columns.raw(
                    "id, driver_id, vehicle_brand, vehicle_model, vehicle_plate, vehicle_type, " +
                    "max_passengers, available_seats, pickup_address, dropoff_address, departure_time, status, notes, " +
                    "users!rides_driver_id_fkey(id, full_name, avatar_url, average_rating, total_trips)"
                )
            ) {
                filter {
                    eq("status", "available")
                }
                order("created_at", Order.DESCENDING)
                limit(10)
            }.decodeList<RemoteRideDto>()

            remoteList.map { dto ->
                val depInstant = runCatching { Instant.parse(dto.departure_time) }.getOrElse { Instant.now().plusSeconds(3600) }
                val vType = if (dto.vehicle_type.equals("motorcycle", ignoreCase = true)) VehicleType.MOTORCYCLE else VehicleType.CAR
                Ride(
                    id = dto.id,
                    driverId = dto.driver_id,
                    driver = User(
                        id = dto.driver_id,
                        firebaseUid = "driver_${dto.driver_id.take(8)}",
                        fullName = dto.users?.full_name ?: "Pengemudi",
                        phoneNumber = "",
                        avatarUrl = dto.users?.avatar_url,
                        averageRating = dto.users?.average_rating ?: 5.0f,
                        totalTrips = dto.users?.total_trips ?: 0,
                        role = UserRole.DRIVER
                    ),
                    vehicleInfo = VehicleInfo(
                        id = dto.id,
                        brand = dto.vehicle_brand,
                        model = dto.vehicle_model,
                        plate = dto.vehicle_plate,
                        type = vType
                    ),
                    maxPassengers = dto.max_passengers,
                    availableSeats = dto.available_seats,
                    pickupAddress = dto.pickup_address,
                    pickupLat = -6.2297,
                    pickupLng = 106.8580,
                    dropoffAddress = dto.dropoff_address,
                    dropoffLat = -6.2250,
                    dropoffLng = 106.8097,
                    departureTime = depInstant,
                    status = RideStatus.AVAILABLE,
                    notes = dto.notes
                )
            }
        }
    }

    override suspend fun getRideById(rideId: String): Result<RideItemUi?> = withContext(Dispatchers.IO) {
        runCatching {
            val dto = supabaseClient.postgrest["rides"].select(
                columns = Columns.raw(
                    "id, driver_id, vehicle_brand, vehicle_model, vehicle_plate, vehicle_type, " +
                    "max_passengers, available_seats, pickup_address, dropoff_address, departure_time, status, notes, " +
                    "users!rides_driver_id_fkey(id, full_name, avatar_url, average_rating, total_trips)"
                )
            ) {
                filter {
                    eq("id", rideId)
                }
            }.decodeSingleOrNull<RemoteRideDto>()

            dto?.let {
                val vType = if (it.vehicle_type.equals("motorcycle", ignoreCase = true)) VehicleType.MOTORCYCLE else VehicleType.CAR
                val depInstant = runCatching { Instant.parse(it.departure_time) }.getOrElse { Instant.now() }
                val depZoned = depInstant.atZone(ZoneId.of("Asia/Jakarta"))
                val arrZoned = depZoned.plusMinutes(40)
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                RideItemUi(
                    id = it.id,
                    driverName = it.users?.full_name ?: "Pengemudi",
                    driverGender = DriverGender.MALE,
                    vehicleModel = "${it.vehicle_brand} ${it.vehicle_model} (${it.vehicle_plate})",
                    vehicleType = vType,
                    departureTimeFormatted = depZoned.format(timeFormatter),
                    arrivalTimeFormatted = arrZoned.format(timeFormatter),
                    availableSeats = it.available_seats,
                    availableSeatsText = if (vType == VehicleType.MOTORCYCLE) "1 Kursi" else "Sisa ${it.available_seats} Kursi",
                    facilities = it.notes?.split(",")?.map { f -> f.trim() }?.filter { f -> f.isNotEmpty() } ?: emptyList(),
                    driverRating = (it.users?.average_rating ?: 5.0f).toDouble(),
                    totalTrips = it.users?.total_trips ?: 0,
                    isOfficeVerified = true
                )
            }
        }
    }
}
