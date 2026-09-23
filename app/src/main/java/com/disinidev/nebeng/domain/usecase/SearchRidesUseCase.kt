package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import javax.inject.Inject

class SearchRidesUseCase @Inject constructor(
    private val rideRepository: RideRepository
) {
    suspend operator fun invoke(
        lat: Double,
        lng: Double,
        radiusMeters: Int = 10000,
        vehicleType: String? = null,
        departureDate: String? = null
    ): Result<List<RideItemUi>> {
        val queryVehicle = if (vehicleType.equals("all", ignoreCase = true)) null else vehicleType
        return rideRepository.searchNearbyRides(
            lat = lat,
            lng = lng,
            radiusMeters = radiusMeters,
            vehicleType = queryVehicle,
            departureDate = departureDate
        )
    }
}
