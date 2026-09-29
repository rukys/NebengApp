package com.disinidev.nebeng.presentation.tripdone

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.repository.BookingResult
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripDoneViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bookingRepository = mockk<BookingRepository>(relaxed = true)
    private val savedStateHandle = SavedStateHandle(mapOf("bookingId" to "booking-123"))
    private lateinit var viewModel: TripDoneViewModel

    @Before
    fun setUp() {
        coEvery {
            bookingRepository.getBookingById("booking-123")
        } answers {
            Result.success(
                BookingResult(
                    bookingId = "booking-123",
                    pickupPin = "123456",
                    driverName = "Andi Pratama",
                    vehicleModel = "Avanza",
                    vehiclePlate = "B 1234 ABC"
                )
            )
        }
        coEvery {
            bookingRepository.rateTrip(any(), any(), any())
        } answers {
            Result.success(Unit)
        }
        viewModel = TripDoneViewModel(savedStateHandle, bookingRepository)
    }

    @Test
    fun `initial state has bookingId from handle, rating 5, and default driver info`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("booking-123", state.bookingId)
        assertEquals("Sampai di Tujuan!", state.title)
        assertEquals("Andi Pratama", state.driverName)
        assertEquals("Avanza", state.vehicleModel)
        assertEquals("B 1234 ABC", state.licensePlate)
        assertEquals(5, state.rating)
        assertEquals("", state.reviewText)
        assertFalse(state.isSubmitting)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `onRatingChanged updates rating`() {
        viewModel.onRatingChanged(4)
        assertEquals(4, viewModel.uiState.value.rating)

        viewModel.onRatingChanged(3)
        assertEquals(3, viewModel.uiState.value.rating)
    }

    @Test
    fun `onReviewTextChanged updates reviewText`() {
        viewModel.onReviewTextChanged("Driver sangat ramah dan mobil bersih.")
        assertEquals("Driver sangat ramah dan mobil bersih.", viewModel.uiState.value.reviewText)
    }

    @Test
    fun `submitRating completes submission and invokes callback`() = runTest {
        var callbackCalled = false

        viewModel.submitRating {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertTrue(viewModel.uiState.value.isCompleted)
        assertFalse(viewModel.uiState.value.isSubmitting)
    }
}
