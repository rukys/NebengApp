package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.data.model.RideSearchResultDto
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
}
