package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.domain.model.TripLocation
import kotlinx.coroutines.flow.Flow

interface TripLocationRepository {
    suspend fun updateDriverLocation(bookingId: String, lat: Double, lng: Double): Result<Unit>
    fun observeDriverLocation(bookingId: String): Flow<TripLocation?>
    suspend fun getDriverLocation(bookingId: String): Result<TripLocation?>
}
