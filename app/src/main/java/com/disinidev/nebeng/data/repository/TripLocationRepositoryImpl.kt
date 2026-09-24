package com.disinidev.nebeng.data.repository

import com.disinidev.nebeng.data.model.TripLocationDto
import com.disinidev.nebeng.domain.model.TripLocation
import com.disinidev.nebeng.domain.repository.TripLocationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripLocationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : TripLocationRepository {

    private val localLocations = ConcurrentHashMap<String, TripLocation>()

    override suspend fun updateDriverLocation(
        bookingId: String,
        lat: Double,
        lng: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val location = TripLocation(
                id = localLocations[bookingId]?.id,
                bookingId = bookingId,
                lat = lat,
                lng = lng,
                updatedAt = Instant.now()
            )
            localLocations[bookingId] = location

            val dto = TripLocationDto(
                bookingId = bookingId,
                lat = lat,
                lng = lng,
                updatedAt = location.updatedAt.toString()
            )

            runCatching {
                supabaseClient.from("trip_locations").upsert(dto)
            }
            Unit
        }
    }

    override fun observeDriverLocation(bookingId: String): Flow<TripLocation?> = flow {
        // Initial emission from cache
        emit(localLocations[bookingId])

        // Periodic poll every 4 seconds
        while (true) {
            delay(4000L)
            val result = getDriverLocation(bookingId)
            val location = result.getOrNull() ?: localLocations[bookingId]
            if (location != null) {
                emit(location)
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getDriverLocation(bookingId: String): Result<TripLocation?> = withContext(Dispatchers.IO) {
        runCatching {
            val remoteDto = runCatching {
                supabaseClient.from("trip_locations")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("booking_id", bookingId)
                        }
                    }
                    .decodeSingleOrNull<TripLocationDto>()
            }.getOrNull()

            if (remoteDto != null) {
                val domain = remoteDto.toDomain()
                localLocations[bookingId] = domain
                domain
            } else {
                localLocations[bookingId]
            }
        }
    }
}
