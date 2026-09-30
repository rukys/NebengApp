package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.domain.repository.UserRepository
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        rideId: String,
        seatPosition: String
    ): Result<BookingResult> {
        val passengerUuid = userRepository.getCurrentUserUuid()
        return bookingRepository.bookSeat(
            rideId = rideId,
            passengerId = passengerUuid,
            seatPosition = seatPosition
        )
    }
}
