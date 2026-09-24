package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.TripLocationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateDriverLocationUseCaseTest {

    private val repository = mockk<TripLocationRepository>()
    private lateinit var useCase: UpdateDriverLocationUseCase

    @Before
    fun setUp() {
        useCase = UpdateDriverLocationUseCase(repository)
    }

    @Test
    fun `invoke with valid coordinates calls repository`() = runTest {
        coEvery { repository.updateDriverLocation("b-123", -6.2215, 106.8065) } returns Result.success(Unit)

        val result = useCase("b-123", -6.2215, 106.8065)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateDriverLocation("b-123", -6.2215, 106.8065) }
    }

    @Test
    fun `invoke with blank bookingId returns failure`() = runTest {
        val result = useCase("", -6.2215, 106.8065)
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke with invalid coordinates returns failure`() = runTest {
        val result = useCase("b-123", 95.0, 106.8065)
        assertTrue(result.isFailure)
    }
}
