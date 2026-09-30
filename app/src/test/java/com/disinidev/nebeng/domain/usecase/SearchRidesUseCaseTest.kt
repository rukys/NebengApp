package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchRidesUseCaseTest {

    private val rideRepository: RideRepository = mockk()
    private lateinit var useCase: SearchRidesUseCase

    @Before
    fun setUp() {
        useCase = SearchRidesUseCase(rideRepository)
    }

    @Test
    fun `invoke calls repository with normalized vehicleType null when all`() = runTest {
        coEvery {
            rideRepository.searchNearbyRides(
                lat = any(),
                lng = any(),
                radiusMeters = any(),
                vehicleType = null,
                departureDate = any()
            )
        } returns Result.success(emptyList())

        val result = useCase(
            lat = -6.2297,
            lng = 106.8580,
            vehicleType = "all"
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            rideRepository.searchNearbyRides(
                lat = -6.2297,
                lng = 106.8580,
                radiusMeters = 10000,
                vehicleType = null,
                departureDate = null
            )
        }
    }

    @Test
    fun `invoke passes specific vehicleType when not all`() = runTest {
        val sampleRide = RideItemUi(
            id = "ride-1",
            driverName = "Andi Pratama",
            vehicleModel = "Toyota Avanza",
            vehicleType = VehicleType.CAR,
            departureTimeFormatted = "07:30",
            arrivalTimeFormatted = "08:10",
            availableSeats = 2,
            availableSeatsText = "Sisa 2 Kursi",
            facilities = listOf("AC", "Non-smoking")
        )

        coEvery {
            rideRepository.searchNearbyRides(
                lat = any(),
                lng = any(),
                radiusMeters = any(),
                vehicleType = "car",
                departureDate = any()
            )
        } returns Result.success(listOf(sampleRide))

        val result = useCase(
            lat = -6.2297,
            lng = 106.8580,
            vehicleType = "car"
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Andi Pratama", result.getOrNull()?.first()?.driverName)
    }
}
