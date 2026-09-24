package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.CreateRideRequest
import com.disinidev.nebeng.domain.repository.RideRepository
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

class CreateRideUseCaseTest {

    private val rideRepository = mockk<RideRepository>()
    private val firebaseAuth = mockk<FirebaseAuth>()
    private val firebaseUser = mockk<FirebaseUser>()
    private lateinit var useCase: CreateRideUseCase

    @Before
    fun setUp() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "driver-456"
        useCase = CreateRideUseCase(rideRepository, firebaseAuth)
    }

    @Test
    fun `invoke with valid parameters succeeds`() = runTest {
        coEvery { rideRepository.createRide(any()) } returns Result.success("ride-new-id")

        val result = useCase(
            pickupAddress = "Tebet Barat",
            pickupLat = -6.234,
            pickupLng = 106.85,
            dropoffAddress = "SCBD Sudirman",
            dropoffLat = -6.225,
            dropoffLng = 106.81,
            vehicleBrand = "Toyota",
            vehicleModel = "Avanza",
            vehiclePlate = "B 1234 ABC",
            vehicleType = "car",
            availableSeats = 3,
            departureTime = "2026-09-24T07:30:00Z",
            notes = "Non-smoking"
        )

        assertTrue(result.isSuccess)
        assertEquals("ride-new-id", result.getOrNull())
    }

    @Test
    fun `invoke with blank pickup address returns failure`() = runTest {
        val result = useCase(
            pickupAddress = "",
            pickupLat = -6.234,
            pickupLng = 106.85,
            dropoffAddress = "SCBD Sudirman",
            dropoffLat = -6.225,
            dropoffLng = 106.81,
            vehicleBrand = "Toyota",
            vehicleModel = "Avanza",
            vehiclePlate = "B 1234 ABC",
            vehicleType = "car",
            availableSeats = 3,
            departureTime = "2026-09-24T07:30:00Z",
            notes = null
        )

        assertTrue(result.isFailure)
        assertEquals("Titik jemput tidak boleh kosong", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with blank plate number returns failure`() = runTest {
        val result = useCase(
            pickupAddress = "Tebet",
            pickupLat = -6.234,
            pickupLng = 106.85,
            dropoffAddress = "SCBD",
            dropoffLat = -6.225,
            dropoffLng = 106.81,
            vehicleBrand = "Toyota",
            vehicleModel = "Avanza",
            vehiclePlate = "",
            vehicleType = "car",
            availableSeats = 2,
            departureTime = "2026-09-24T07:30:00Z",
            notes = null
        )

        assertTrue(result.isFailure)
        assertEquals("Plat nomor kendaraan harus diisi", result.exceptionOrNull()?.message)
    }
}
