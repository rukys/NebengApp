package com.disinidev.nebeng.presentation.search.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.usecase.SearchRidesUseCase
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import com.disinidev.nebeng.presentation.search.model.SearchFilterOptions
import com.disinidev.nebeng.presentation.search.model.SortBy
import com.disinidev.nebeng.presentation.search.model.VehicleFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResultsUiState(
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val departureTime: String = "",
    val selectedFilterTab: VehicleFilter = VehicleFilter.ALL,
    val activeFilterOptions: SearchFilterOptions = SearchFilterOptions(),
    val draftFilterOptions: SearchFilterOptions = SearchFilterOptions(),
    val allRides: List<RideItemUi> = emptyList(),
    val displayedRides: List<RideItemUi> = emptyList(),
    val totalCount: Int = 0,
    val carCount: Int = 0,
    val motorCount: Int = 0,
    val isFilterSheetOpen: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchRidesUseCase: SearchRidesUseCase
) : ViewModel() {

    private val navPickup: String = savedStateHandle.get<String>("pickupAddress") ?: ""
    private val navDropoff: String = savedStateHandle.get<String>("dropoffAddress") ?: ""
    private val navDepartureTime: String = savedStateHandle.get<String>("departureTime") ?: ""
    private val navVehicleType: String = savedStateHandle.get<String>("vehicleType") ?: "all"
    private val navPickupLat: Double = savedStateHandle.get<Double>("pickupLat") ?: -6.2297
    private val navPickupLng: Double = savedStateHandle.get<Double>("pickupLng") ?: 106.8580

    private val initialTab = when (navVehicleType.lowercase()) {
        "car" -> VehicleFilter.CAR
        "motorcycle" -> VehicleFilter.MOTORCYCLE
        else -> VehicleFilter.ALL
    }

    private val _uiState = MutableStateFlow(
        SearchResultsUiState(
            pickupAddress = navPickup,
            dropoffAddress = navDropoff,
            departureTime = navDepartureTime,
            selectedFilterTab = initialTab,
            allRides = emptyList()
        )
    )
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    init {
        applyFilters()
        loadRides()
    }

    fun loadRides() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = searchRidesUseCase(
                lat = navPickupLat,
                lng = navPickupLng,
                radiusMeters = 10000,
                vehicleType = if (navVehicleType == "all") null else navVehicleType
            )

            result.fold(
                onSuccess = { rides ->
                    _uiState.update {
                        it.copy(
                            allRides = rides,
                            totalCount = rides.size,
                            carCount = rides.count { r -> r.vehicleType == VehicleType.CAR },
                            motorCount = rides.count { r -> r.vehicleType == VehicleType.MOTORCYCLE },
                            isLoading = false
                        )
                    }
                    applyFilters()
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            allRides = emptyList(),
                            totalCount = 0,
                            carCount = 0,
                            motorCount = 0,
                            isLoading = false
                        )
                    }
                    applyFilters()
                }
            )
        }
    }

    fun onTabFilterSelected(filter: VehicleFilter) {
        _uiState.update { it.copy(selectedFilterTab = filter) }
        applyFilters()
    }

    fun onDraftFilterChanged(options: SearchFilterOptions) {
        _uiState.update { it.copy(draftFilterOptions = options) }
    }

    fun onResetFilter() {
        val defaultOptions = SearchFilterOptions()
        _uiState.update {
            it.copy(
                draftFilterOptions = defaultOptions,
                activeFilterOptions = defaultOptions
            )
        }
        applyFilters()
    }

    fun onApplyFilter() {
        _uiState.update {
            it.copy(
                activeFilterOptions = it.draftFilterOptions,
                isFilterSheetOpen = false
            )
        }
        applyFilters()
    }

    fun openFilterSheet() {
        _uiState.update {
            it.copy(
                draftFilterOptions = it.activeFilterOptions,
                isFilterSheetOpen = true
            )
        }
    }

    fun closeFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
    }

    private fun applyFilters() {
        _uiState.update { state ->
            val tab = state.selectedFilterTab
            val filter = state.activeFilterOptions

            var result = state.allRides

            // 1. Vehicle filter from tab or sheet
            result = when (tab) {
                VehicleFilter.ALL -> {
                    if (filter.vehicleType != null) {
                        result.filter { it.vehicleType == filter.vehicleType }
                    } else {
                        result
                    }
                }
                VehicleFilter.CAR -> result.filter { it.vehicleType == VehicleType.CAR }
                VehicleFilter.MOTORCYCLE -> result.filter { it.vehicleType == VehicleType.MOTORCYCLE }
                VehicleFilter.FASTEST -> result // Handled by sort
            }

            // 2. Driver Gender filter (Sesuai PRD F-02)
            if (filter.driverGender != DriverGender.ALL) {
                result = result.filter { it.driverGender == filter.driverGender }
            }

            // 3. Rating filter
            if (filter.minRating != null) {
                result = result.filter { it.driverRating >= filter.minRating }
            }

            // 4. Destination matching & prioritization
            if (navDropoff.isNotBlank()) {
                val dropoffKeywords = navDropoff.lowercase().split(" ", ",", "-")
                    .filter { it.length >= 3 }
                if (dropoffKeywords.isNotEmpty()) {
                    val matching = result.filter { ride ->
                        val rDropoff = ride.dropoffAddress.lowercase()
                        dropoffKeywords.any { kw -> rDropoff.contains(kw) }
                    }
                    if (matching.isNotEmpty()) {
                        val nonMatching = result.filter { it !in matching }
                        result = matching + nonMatching
                    }
                }
            }

            // 5. Sorting
            result = when {
                tab == VehicleFilter.FASTEST || filter.sortBy == SortBy.FASTEST ->
                    result.sortedBy { it.departureTimeFormatted }
                filter.sortBy == SortBy.MOST_SEATS ->
                    result.sortedByDescending { it.availableSeats }
                filter.sortBy == SortBy.HIGHEST_RATING ->
                    result.sortedByDescending { it.driverRating }
                filter.sortBy == SortBy.CLOSEST_TIME ->
                    result.sortedBy { it.departureTimeFormatted }
                else -> result
            }

            val carCount = state.allRides.count { it.vehicleType == VehicleType.CAR }
            val motorCount = state.allRides.count { it.vehicleType == VehicleType.MOTORCYCLE }

            state.copy(
                displayedRides = result,
                totalCount = state.allRides.size,
                carCount = carCount,
                motorCount = motorCount
            )
        }
    }
}
