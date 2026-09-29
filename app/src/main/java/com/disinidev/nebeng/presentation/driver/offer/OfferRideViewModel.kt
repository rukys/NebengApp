package com.disinidev.nebeng.presentation.driver.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.PlaceSuggestion
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.LocationSearchRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.domain.repository.VehicleRepository
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
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private fun calculateDefaultDepartureTime(): String {
    val now = LocalTime.now(ZoneId.of("Asia/Jakarta")).plusMinutes(30)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return "Hari Ini, ${now.format(formatter)}"
}

data class OfferRideUiState(
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val pickupLat: Double = -6.2297,
    val pickupLng: Double = 106.8580,
    val dropoffLat: Double = -6.2250,
    val dropoffLng: Double = 106.8097,
    val vehicleType: String = "car", // "car" or "motorcycle"
    val vehicleModel: String = "",
    val vehiclePlate: String = "",
    val availableSeats: Int = 3,
    val departureTime: String = calculateDefaultDepartureTime(),
    val notes: String = "",
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val activeField: ActiveSearchField = ActiveSearchField.NONE,
    val isSearchingPlaces: Boolean = false,
    val isPublishing: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val savedVehicles: List<VehicleInfo> = emptyList(),
    val selectedVehicleId: String? = null
)

@HiltViewModel
class OfferRideViewModel @Inject constructor(
    private val createRideUseCase: CreateRideUseCase,
    private val locationSearchRepository: LocationSearchRepository,
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfferRideUiState())
    val uiState: StateFlow<OfferRideUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSavedVehicles()
    }

    fun loadSavedVehicles() {
        viewModelScope.launch {
            try {
                val userUuid = userRepository.getCurrentUserUuid()
                vehicleRepository.getDriverVehicles(userUuid)
                    .onSuccess { vehicles ->
                        _uiState.update { it.copy(savedVehicles = vehicles) }
                        if (vehicles.isNotEmpty() && _uiState.value.selectedVehicleId == null) {
                            val matching = vehicles.firstOrNull {
                                (it.type == VehicleType.CAR && _uiState.value.vehicleType == "car") ||
                                (it.type == VehicleType.MOTORCYCLE && _uiState.value.vehicleType == "motorcycle")
                            } ?: vehicles.first()
                            selectSavedVehicle(matching)
                        }
                    }
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    fun selectSavedVehicle(vehicle: VehicleInfo) {
        _uiState.update {
            val isMotorcycle = vehicle.type == VehicleType.MOTORCYCLE
            it.copy(
                selectedVehicleId = vehicle.id,
                vehicleType = if (isMotorcycle) "motorcycle" else "car",
                vehicleModel = "${vehicle.brand} ${vehicle.model}",
                vehiclePlate = vehicle.plate,
                availableSeats = if (isMotorcycle) 1 else if (it.availableSeats > 1) it.availableSeats else 3
            )
        }
    }

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
        val matchingVehicle = _uiState.value.savedVehicles.firstOrNull {
            if (type == "motorcycle") it.type == VehicleType.MOTORCYCLE else it.type == VehicleType.CAR
        }

        _uiState.update {
            if (matchingVehicle != null) {
                it.copy(
                    vehicleType = type,
                    selectedVehicleId = matchingVehicle.id,
                    vehicleModel = "${matchingVehicle.brand} ${matchingVehicle.model}",
                    vehiclePlate = matchingVehicle.plate,
                    availableSeats = if (type == "motorcycle") 1 else 3
                )
            } else {
                it.copy(
                    vehicleType = type,
                    selectedVehicleId = null,
                    availableSeats = if (type == "motorcycle") 1 else 3,
                    vehicleModel = "",
                    vehiclePlate = ""
                )
            }
        }
    }

    fun onVehicleModelChange(model: String) {
        _uiState.update { it.copy(vehicleModel = model, selectedVehicleId = null) }
    }

    fun onVehiclePlateChange(plate: String) {
        _uiState.update { it.copy(vehiclePlate = plate, selectedVehicleId = null) }
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

        if (state.pickupAddress.isBlank() || state.dropoffAddress.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Titik jemput dan tujuan belum diisi. Tentukan rute perjalananmu.") }
            return
        }
        if (state.vehicleModel.isBlank() || state.vehiclePlate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Data kendaraan belum lengkap. Pilih kendaraan terdaftar atau isi data kendaraan.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, errorMessage = null) }
            try {
                val brand = state.vehicleModel.split(" ").firstOrNull()?.ifBlank { "Kendaraan" } ?: "Kendaraan"
                val result = createRideUseCase(
                    pickupAddress = state.pickupAddress,
                    pickupLat = state.pickupLat,
                    pickupLng = state.pickupLng,
                    dropoffAddress = state.dropoffAddress,
                    dropoffLat = state.dropoffLat,
                    dropoffLng = state.dropoffLng,
                    vehicleBrand = brand,
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
                        _uiState.update {
                            it.copy(
                                isPublishing = false,
                                errorMessage = "Gagal mempublikasikan tebengan. ${error.message ?: "Periksa koneksi internet dan coba lagi."}"
                            )
                        }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isPublishing = false,
                        errorMessage = "Gagal mempublikasikan tebengan. Terjadi gangguan koneksi. Coba lagi dalam beberapa saat."
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
