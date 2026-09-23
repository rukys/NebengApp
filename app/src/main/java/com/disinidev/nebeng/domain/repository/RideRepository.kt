package com.disinidev.nebeng.domain.repository

import com.disinidev.nebeng.presentation.search.model.RideItemUi

interface RideRepository {
    suspend fun searchNearbyRides(
        lat: Double,
        lng: Double,
        radiusMeters: Int = 10000,
        vehicleType: String? = null,
        departureDate: String? = null
    ): Result<List<RideItemUi>>
}
