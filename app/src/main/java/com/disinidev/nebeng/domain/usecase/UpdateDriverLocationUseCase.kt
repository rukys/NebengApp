package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.TripLocationRepository
import javax.inject.Inject

class UpdateDriverLocationUseCase @Inject constructor(
    private val tripLocationRepository: TripLocationRepository
) {
    suspend operator fun invoke(bookingId: String, lat: Double, lng: Double): Result<Unit> {
        if (bookingId.isBlank()) {
            return Result.failure(IllegalArgumentException("Booking ID cannot be blank"))
        }
        if (lat !in -90.0..90.0 || lng !in -180.0..180.0) {
            return Result.failure(IllegalArgumentException("Invalid latitude or longitude coordinates"))
        }
        return tripLocationRepository.updateDriverLocation(bookingId, lat, lng)
    }
}
