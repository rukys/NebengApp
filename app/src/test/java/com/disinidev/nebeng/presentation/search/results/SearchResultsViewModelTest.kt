package com.disinidev.nebeng.presentation.search.results

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.usecase.SearchRidesUseCase
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.SearchFilterOptions
import com.disinidev.nebeng.presentation.search.model.SortBy
import com.disinidev.nebeng.presentation.search.model.VehicleFilter
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchResultsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchRidesUseCase: SearchRidesUseCase = mockk()
    private lateinit var viewModel: SearchResultsViewModel

    @Before
    fun setUp() {
        coEvery {
            searchRidesUseCase.invoke(any(), any(), any(), any(), any())
        } returns Result.success(emptyList())

        val savedStateHandle = SavedStateHandle(
            mapOf(
                "pickupAddress" to "Stasiun Tebet",
                "dropoffAddress" to "SCBD Lot 8",
                "vehicleType" to "all",
                "pickupLat" to -6.2297,
                "pickupLng" to 106.8580
            )
        )
        viewModel = SearchResultsViewModel(savedStateHandle, searchRidesUseCase)
    }

    @Test
    fun `initial state loads all rides and correct addresses`() {
        val state = viewModel.uiState.value
        assertEquals("Stasiun Tebet", state.pickupAddress)
        assertEquals("SCBD Lot 8", state.dropoffAddress)
        assertEquals(VehicleFilter.ALL, state.selectedFilterTab)
        assertTrue(state.displayedRides.isNotEmpty())
    }

    @Test
    fun `selecting CAR filter tab filters only cars`() {
        viewModel.onTabFilterSelected(VehicleFilter.CAR)
        val state = viewModel.uiState.value
        assertEquals(VehicleFilter.CAR, state.selectedFilterTab)
        assertTrue(state.displayedRides.all { it.vehicleType == VehicleType.CAR })
    }

    @Test
    fun `selecting MOTORCYCLE filter tab filters only motorcycles`() {
        viewModel.onTabFilterSelected(VehicleFilter.MOTORCYCLE)
        val state = viewModel.uiState.value
        assertEquals(VehicleFilter.MOTORCYCLE, state.selectedFilterTab)
        assertTrue(state.displayedRides.all { it.vehicleType == VehicleType.MOTORCYCLE })
    }

    @Test
    fun `filter sheet open and close update ui state`() {
        viewModel.openFilterSheet()
        assertTrue(viewModel.uiState.value.isFilterSheetOpen)

        viewModel.closeFilterSheet()
        assertFalse(viewModel.uiState.value.isFilterSheetOpen)
    }

    @Test
    fun `apply filter updates active filter and closes sheet`() {
        viewModel.openFilterSheet()
        viewModel.onDraftFilterChanged(
            SearchFilterOptions(
                sortBy = SortBy.MOST_SEATS,
                minRating = 4.9
            )
        )
        viewModel.onApplyFilter()

        val state = viewModel.uiState.value
        assertFalse(state.isFilterSheetOpen)
        assertEquals(SortBy.MOST_SEATS, state.activeFilterOptions.sortBy)
        assertEquals(4.9, state.activeFilterOptions.minRating)
        assertTrue(state.displayedRides.all { it.driverRating >= 4.9 })
    }

    @Test
    fun `reset filter restores default options`() {
        viewModel.onDraftFilterChanged(SearchFilterOptions(sortBy = SortBy.MOST_SEATS, minRating = 5.0))
        viewModel.onApplyFilter()

        viewModel.onResetFilter()
        val state = viewModel.uiState.value
        assertEquals(SortBy.FASTEST, state.activeFilterOptions.sortBy)
        assertEquals(4.8, state.activeFilterOptions.minRating)
    }

    @Test
    fun `filtering by FEMALE driver returns only female drivers`() {
        viewModel.onDraftFilterChanged(
            SearchFilterOptions(
                driverGender = DriverGender.FEMALE
            )
        )
        viewModel.onApplyFilter()

        val state = viewModel.uiState.value
        assertTrue(state.displayedRides.isNotEmpty())
        assertTrue(state.displayedRides.all { it.driverGender == DriverGender.FEMALE })
    }
}
