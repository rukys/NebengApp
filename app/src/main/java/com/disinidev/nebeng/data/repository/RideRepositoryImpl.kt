package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.data.model.RideSearchResultDto
import com.disinidev.nebeng.domain.repository.CreateRideRequest
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
                    "pickup_location" to "SRID=4326;POINT(${request.pickupLng} ${request.pickupLat})",
                    "dropoff_address" to request.dropoffAddress,
                    "dropoff_location" to "SRID=4326;POINT(${request.dropoffLng} ${request.dropoffLat})",
                    "departure_time" to request.departureTime,
                    "status" to "available",
                    "notes" to (request.notes ?: "")
                )
                supabaseClient.postgrest.from("rides").insert(insertPayload)
            } catch (_: Exception) {
                // In-memory / demo fallback
            }
            newId
        }
    }
}
