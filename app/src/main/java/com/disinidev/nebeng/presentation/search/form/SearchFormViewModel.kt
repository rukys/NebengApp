package com.disinidev.nebeng.presentation.search.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.PlaceSuggestion
import com.disinidev.nebeng.domain.repository.LocationSearchRepository
import com.disinidev.nebeng.presentation.search.model.FavoriteLocation
import com.disinidev.nebeng.presentation.search.model.SearchHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ActiveSearchField {
    NONE, ORIGIN, DESTINATION
}

data class SearchFormUiState(
    val origin: String = "",
    val destination: String = "",
    val originLatitude: Double? = null,
    val originLongitude: Double? = null,
    val destinationLatitude: Double? = null,
    val destinationLongitude: Double? = null,
    val selectedTime: String = "Hari Ini, 07:30",
    val seatCount: Int = 1,
    val vehicleType: String = "car",
    val favoriteLocations: List<FavoriteLocation> = emptyList(),
    val recentSearches: List<SearchHistoryItem> = emptyList(),
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val activeField: ActiveSearchField = ActiveSearchField.NONE,
    val isSearchingPlaces: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SearchFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val locationSearchRepository: LocationSearchRepository
) : ViewModel() {

    private val initialVehicleType: String = savedStateHandle.get<String>("vehicleType") ?: "car"
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(
        SearchFormUiState(
            vehicleType = initialVehicleType,
            favoriteLocations = emptyList(),
            recentSearches = emptyList()
        )
    )
    val uiState: StateFlow<SearchFormUiState> = _uiState.asStateFlow()

    fun onOriginChange(origin: String) {
        _uiState.update {
            it.copy(
                origin = origin,
                activeField = ActiveSearchField.ORIGIN,
                errorMessage = null
            )
        }
        performPlaceSearch(origin)
    }

    fun onDestinationChange(destination: String) {
        _uiState.update {
            it.copy(
                destination = destination,
                activeField = ActiveSearchField.DESTINATION,
                errorMessage = null
            )
        }
        performPlaceSearch(destination)
    }

    private fun performPlaceSearch(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearchingPlaces = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce 400ms for OSM Nominatim policy
            _uiState.update { it.copy(isSearchingPlaces = true) }
            val results = locationSearchRepository.searchPlaces(query)
            _uiState.update {
                it.copy(
                    suggestions = results,
                    isSearchingPlaces = false
                )
            }
        }
    }

    fun onSelectSuggestion(suggestion: PlaceSuggestion) {
        _uiState.update { state ->
            when (state.activeField) {
                ActiveSearchField.ORIGIN -> {
                    state.copy(
                        origin = suggestion.name,
                        originLatitude = suggestion.latitude,
                        originLongitude = suggestion.longitude,
                        suggestions = emptyList(),
                        activeField = ActiveSearchField.NONE
                    )
                }
                ActiveSearchField.DESTINATION -> {
                    state.copy(
                        destination = suggestion.name,
                        destinationLatitude = suggestion.latitude,
                        destinationLongitude = suggestion.longitude,
                        suggestions = emptyList(),
                        activeField = ActiveSearchField.NONE
                    )
                }
                ActiveSearchField.NONE -> state.copy(suggestions = emptyList())
            }
        }
    }

    fun onDismissSuggestions() {
        searchJob?.cancel()
        _uiState.update { it.copy(suggestions = emptyList(), activeField = ActiveSearchField.NONE) }
    }

    fun onSwapLocations() {
        _uiState.update {
            it.copy(
                origin = it.destination,
                destination = it.origin,
                originLatitude = it.destinationLatitude,
                originLongitude = it.destinationLongitude,
                destinationLatitude = it.originLatitude,
                destinationLongitude = it.originLongitude,
                suggestions = emptyList(),
                activeField = ActiveSearchField.NONE
            )
        }
    }

    fun onClearOrigin() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                origin = "",
                originLatitude = null,
                originLongitude = null,
                suggestions = emptyList()
            )
        }
    }

    fun onClearDestination() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                destination = "",
                destinationLatitude = null,
                destinationLongitude = null,
                suggestions = emptyList()
            )
        }
    }

    fun onSelectFavorite(favorite: FavoriteLocation) {
        onDismissSuggestions()
        _uiState.update {
            if (it.origin.isBlank()) {
                it.copy(origin = favorite.address)
            } else {
                it.copy(destination = favorite.address)
            }
        }
    }

    fun onSelectHistory(history: SearchHistoryItem) {
        onDismissSuggestions()
        _uiState.update {
            it.copy(
                origin = history.origin,
                destination = history.destination
            )
        }
    }

    fun onSeatCountChange(count: Int) {
        if (count in 1..4) {
            _uiState.update { it.copy(seatCount = count) }
        }
    }

    fun onVehicleTypeChange(type: String) {
        _uiState.update { it.copy(vehicleType = type) }
    }

    fun onTimeChange(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }
}
