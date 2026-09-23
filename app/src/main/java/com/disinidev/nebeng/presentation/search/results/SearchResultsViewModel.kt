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
    val pickupAddress: String = "Stasiun Tebet (Pintu Barat)",
    val dropoffAddress: String = "SCBD Sudirman (Lot 8 & Pasific)",
    val departureTime: String = "Hari Ini, 07:30",
    val selectedFilterTab: VehicleFilter = VehicleFilter.ALL,
    val activeFilterOptions: SearchFilterOptions = SearchFilterOptions(),
    val draftFilterOptions: SearchFilterOptions = SearchFilterOptions(),
    val allRides: List<RideItemUi> = emptyList(),
    val displayedRides: List<RideItemUi> = emptyList(),
    val totalCount: Int = 18,
    val carCount: Int = 12,
    val motorCount: Int = 6,
    val isFilterSheetOpen: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchRidesUseCase: SearchRidesUseCase
) : ViewModel() {

    private val navPickup: String = savedStateHandle.get<String>("pickupAddress") ?: "Stasiun Tebet (Pintu Barat)"
    private val navDropoff: String = savedStateHandle.get<String>("dropoffAddress") ?: "SCBD Sudirman (Lot 8 & Pasific)"
    private val navDepartureTime: String = savedStateHandle.get<String>("departureTime") ?: "Hari Ini, 07:30"
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
            allRides = createMockRides()
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
                    val finalRides = if (rides.isNotEmpty()) rides else createMockRides()
                    _uiState.update {
                        it.copy(
                            allRides = finalRides,
                            totalCount = finalRides.size,
                            carCount = finalRides.count { r -> r.vehicleType == VehicleType.CAR },
                            motorCount = finalRides.count { r -> r.vehicleType == VehicleType.MOTORCYCLE },
                            isLoading = false
                        )
                    }
                    applyFilters()
                },
                onFailure = {
                    val fallback = createMockRides()
                    _uiState.update {
                        it.copy(
                            allRides = fallback,
                            totalCount = fallback.size,
                            carCount = fallback.count { r -> r.vehicleType == VehicleType.CAR },
                            motorCount = fallback.count { r -> r.vehicleType == VehicleType.MOTORCYCLE },
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

            // 4. Sorting
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

    private fun createMockRides(): List<RideItemUi> {
        return listOf(
            RideItemUi(
                id = "ride_1",
                driverName = "Andi Pratama",
                driverGender = DriverGender.MALE,
                vehicleModel = "Avanza",
                vehicleType = VehicleType.CAR,
                departureTimeFormatted = "07:30 WIB",
                arrivalTimeFormatted = "07:55",
                availableSeats = 2,
                availableSeatsText = "Sisa 2 kursi",
                facilities = listOf("Sisa 2 kursi", "AC Dingin", "Non-Smoking"),
                driverRating = 4.9,
                totalTrips = 120
            ),
            RideItemUi(
                id = "ride_2",
                driverName = "Reza Hendra",
                driverGender = DriverGender.MALE,
                vehicleModel = "NMAX",
                vehicleType = VehicleType.MOTORCYCLE,
                departureTimeFormatted = "07:45 WIB",
                arrivalTimeFormatted = "08:05",
                availableSeats = 1,
                availableSeatsText = "1 slot",
                facilities = listOf("1 slot", "Helm SNI & Jas Hujan"),
                driverRating = 4.8,
                totalTrips = 85
            ),
            RideItemUi(
                id = "ride_3",
                driverName = "Bambang S.",
                driverGender = DriverGender.MALE,
                vehicleModel = "Innova",
                vehicleType = VehicleType.CAR,
                departureTimeFormatted = "08:00 WIB",
                arrivalTimeFormatted = "08:25",
                availableSeats = 3,
                availableSeatsText = "Sisa 3 kursi",
                facilities = listOf("Sisa 3 kursi", "Bagasi Luas"),
                driverRating = 4.9,
                totalTrips = 210
            ),
            RideItemUi(
                id = "ride_4",
                driverName = "Dian Sastrowardoyo",
                driverGender = DriverGender.FEMALE,
                vehicleModel = "Yaris Cross",
                vehicleType = VehicleType.CAR,
                departureTimeFormatted = "08:15 WIB",
                arrivalTimeFormatted = "08:40",
                availableSeats = 2,
                availableSeatsText = "Sisa 2 kursi",
                facilities = listOf("Sisa 2 kursi", "AC Dingin", "Music on Request"),
                driverRating = 5.0,
                totalTrips = 64
            ),
            RideItemUi(
                id = "ride_5",
                driverName = "Siti Rahmawati",
                driverGender = DriverGender.FEMALE,
                vehicleModel = "Scoopy",
                vehicleType = VehicleType.MOTORCYCLE,
                departureTimeFormatted = "08:20 WIB",
                arrivalTimeFormatted = "08:42",
                availableSeats = 1,
                availableSeatsText = "1 slot",
                facilities = listOf("1 slot", "Helm Bersih & Wangi"),
                driverRating = 4.9,
                totalTrips = 140
            ),
            RideItemUi(
                id = "ride_6",
                driverName = "Hendra Gunawan",
                driverGender = DriverGender.MALE,
                vehicleModel = "Sigra",
                vehicleType = VehicleType.CAR,
                departureTimeFormatted = "08:30 WIB",
                arrivalTimeFormatted = "08:55",
                availableSeats = 3,
                availableSeatsText = "Sisa 3 kursi",
                facilities = listOf("Sisa 3 kursi", "Non-Smoking"),
                driverRating = 4.8,
                totalTrips = 98
            )
        )
    }
}
