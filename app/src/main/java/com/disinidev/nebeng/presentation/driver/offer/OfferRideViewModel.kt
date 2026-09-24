package com.disinidev.nebeng.presentation.driver.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.PlaceSuggestion
import com.disinidev.nebeng.domain.repository.LocationSearchRepository
import com.disinidev.nebeng.domain.usecase.CreateRideUseCase
import com.disinidev.nebeng.presentation.search.form.ActiveSearchField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OfferRideUiState(
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val pickupLat: Double = -6.2297,
    val pickupLng: Double = 106.8580,
    val dropoffLat: Double = -6.2250,
    val dropoffLng: Double = 106.8097,
    val vehicleType: String = "car", // "car" or "motorcycle"
    val vehicleModel: String = "Toyota Avanza Silver",
    val vehiclePlate: String = "B 1234 ABC",
    val availableSeats: Int = 3,
    val departureTime: String = "Hari Ini, 07:30",
    val notes: String = "",
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val activeField: ActiveSearchField = ActiveSearchField.NONE,
    val isSearchingPlaces: Boolean = false,
    val isPublishing: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OfferRideViewModel @Inject constructor(
    private val createRideUseCase: CreateRideUseCase,
    private val locationSearchRepository: LocationSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfferRideUiState())
    val uiState: StateFlow<OfferRideUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onPickupChange(pickup: String) {
        _uiState.update {
            it.copy(
                pickupAddress = pickup,
                activeField = ActiveSearchField.ORIGIN,
                errorMessage = null
            )
        }
        querySuggestions(pickup)
    }

    fun onDropoffChange(dropoff: String) {
        _uiState.update {
            it.copy(
                dropoffAddress = dropoff,
                activeField = ActiveSearchField.DESTINATION,
                errorMessage = null
            )
        }
        querySuggestions(dropoff)
    }

    fun selectSuggestion(suggestion: PlaceSuggestion) {
        val currentField = _uiState.value.activeField
        _uiState.update {
            when (currentField) {
                ActiveSearchField.ORIGIN -> it.copy(
                    pickupAddress = suggestion.name,
                    pickupLat = suggestion.latitude,
                    pickupLng = suggestion.longitude,
                    suggestions = emptyList(),
                    activeField = ActiveSearchField.NONE
                )
                ActiveSearchField.DESTINATION -> it.copy(
                    dropoffAddress = suggestion.name,
                    dropoffLat = suggestion.latitude,
                    dropoffLng = suggestion.longitude,
                    suggestions = emptyList(),
                    activeField = ActiveSearchField.NONE
                )
                ActiveSearchField.NONE -> it.copy(suggestions = emptyList())
            }
        }
    }

    fun closeSuggestions() {
        _uiState.update { it.copy(suggestions = emptyList(), activeField = ActiveSearchField.NONE) }
    }

    fun onVehicleTypeChange(type: String) {
        _uiState.update {
            it.copy(
                vehicleType = type,
                availableSeats = if (type == "motorcycle") 1 else 3,
                vehicleModel = if (type == "motorcycle") "Yamaha NMAX Hitam" else "Toyota Avanza Silver",
                vehiclePlate = if (type == "motorcycle") "B 5678 XYZ" else "B 1234 ABC"
            )
        }
    }

    fun onVehicleModelChange(model: String) {
        _uiState.update { it.copy(vehicleModel = model) }
    }

    fun onVehiclePlateChange(plate: String) {
        _uiState.update { it.copy(vehiclePlate = plate) }
    }

    fun onDepartureTimeChange(time: String) {
        _uiState.update { it.copy(departureTime = time) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun incrementSeats() {
        val max = if (_uiState.value.vehicleType == "motorcycle") 1 else 6
        if (_uiState.value.availableSeats < max) {
            _uiState.update { it.copy(availableSeats = it.availableSeats + 1) }
        }
    }

    fun decrementSeats() {
        if (_uiState.value.availableSeats > 1) {
            _uiState.update { it.copy(availableSeats = it.availableSeats - 1) }
        }
    }

    private fun querySuggestions(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(suggestions = emptyList(), isSearchingPlaces = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isSearchingPlaces = true) }
            try {
                val results = locationSearchRepository.searchPlaces(query.trim())
                _uiState.update { it.copy(suggestions = results, isSearchingPlaces = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(suggestions = emptyList(), isSearchingPlaces = false) }
            }
        }
    }

    fun publishRide(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.isPublishing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, errorMessage = null) }
            val result = createRideUseCase(
                pickupAddress = state.pickupAddress,
                pickupLat = state.pickupLat,
                pickupLng = state.pickupLng,
                dropoffAddress = state.dropoffAddress,
                dropoffLat = state.dropoffLat,
                dropoffLng = state.dropoffLng,
                vehicleBrand = state.vehicleModel.split(" ").firstOrNull() ?: "Toyota",
                vehicleModel = state.vehicleModel,
                vehiclePlate = state.vehiclePlate,
                vehicleType = state.vehicleType,
                availableSeats = state.availableSeats,
                departureTime = state.departureTime,
                notes = state.notes
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isPublishing = false, isSuccess = true) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isPublishing = false, errorMessage = error.message) }
                }
            )
        }
    }
}
