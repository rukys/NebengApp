package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(
        rideId: String,
        seatPosition: String
    ): Result<BookingResult> {
        val passengerId = firebaseAuth.currentUser?.uid ?: "00000000-0000-0000-0000-000000000001"
        return bookingRepository.bookSeat(
            rideId = rideId,
            passengerId = passengerId,
            seatPosition = seatPosition
        )
    }
}
