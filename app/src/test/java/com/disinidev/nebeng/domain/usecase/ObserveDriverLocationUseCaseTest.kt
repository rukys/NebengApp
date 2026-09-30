package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.model.TripLocation
import com.disinidev.nebeng.domain.repository.TripLocationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ObserveDriverLocationUseCaseTest {

    private val repository = mockk<TripLocationRepository>()
    private lateinit var useCase: ObserveDriverLocationUseCase

    @Before
    fun setUp() {
        useCase = ObserveDriverLocationUseCase(repository)
    }

    @Test
    fun `invoke returns flow of trip location from repository`() = runTest {
        val expected = TripLocation(
            id = "loc-1",
            bookingId = "b-123",
            lat = -6.2215,
            lng = 106.8065,
            updatedAt = Instant.now()
        )
        every { repository.observeDriverLocation("b-123") } returns flowOf(expected)

        val result = useCase("b-123").first()

        assertEquals(expected, result)
    }
}
