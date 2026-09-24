package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.model.TripLocation
import com.disinidev.nebeng.domain.repository.TripLocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDriverLocationUseCase @Inject constructor(
    private val tripLocationRepository: TripLocationRepository
) {
    operator fun invoke(bookingId: String): Flow<TripLocation?> {
        return tripLocationRepository.observeDriverLocation(bookingId)
    }
}
