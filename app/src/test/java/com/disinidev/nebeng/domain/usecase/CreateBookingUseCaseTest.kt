package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateBookingUseCaseTest {

    private val bookingRepository = mockk<BookingRepository>()
    private val firebaseAuth = mockk<FirebaseAuth>()
    private val firebaseUser = mockk<FirebaseUser>()
    private lateinit var useCase: CreateBookingUseCase

    @Before
    fun setUp() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "user-123"
        useCase = CreateBookingUseCase(bookingRepository, firebaseAuth)
    }

    @Test
    fun `invoke calls repository and returns success`() = runTest {
        val expected = BookingResult(
            bookingId = "booking-999",
            pickupPin = "123 456",
            driverName = "Andi Pratama",
            vehicleModel = "Avanza",
            vehiclePlate = "B 1234 ABC",
            seatPosition = "front_left"
        )
        coEvery {
            bookingRepository.bookSeat("ride-1", "user-123", "front_left")
        } returns Result.success(expected)

        val result = useCase("ride-1", "front_left")

        assertTrue(result.isSuccess)
        assertEquals("booking-999", result.getOrNull()?.bookingId)
        assertEquals("123 456", result.getOrNull()?.pickupPin)
    }

    @Test
    fun `invoke propagates failure when repository fails`() = runTest {
        coEvery {
            bookingRepository.bookSeat(any(), any(), any())
        } returns Result.failure(IllegalStateException("Kursi sudah penuh"))

        val result = useCase("ride-1", "front_left")

        assertTrue(result.isFailure)
        assertEquals("Kursi sudah penuh", result.exceptionOrNull()?.message)
    }
}
