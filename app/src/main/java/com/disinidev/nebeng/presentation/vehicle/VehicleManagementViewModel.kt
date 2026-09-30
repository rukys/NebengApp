package com.disinidev.nebeng.presentation.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleManagementViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleManagementUiState())
    val uiState: StateFlow<VehicleManagementUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
    }

    fun loadVehicles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val driverId = userRepository.getCurrentUserUuid()
            vehicleRepository.getDriverVehicles(driverId)
                .onSuccess { list ->
                    _uiState.update { it.copy(vehicles = list, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    // --- Form events ---

    fun openAddSheet() {
        _uiState.update {
            it.copy(
                isAddSheetOpen = true,
                formBrand = "",
                formModel = "",
                formPlate = "",
                formColor = "",
                formYear = "",
                formType = VehicleType.CAR,
                formError = null
            )
        }
    }

    fun closeAddSheet() {
        _uiState.update { it.copy(isAddSheetOpen = false, formError = null) }
    }

    fun onBrandChange(v: String) = _uiState.update { it.copy(formBrand = v, formError = null) }
    fun onModelChange(v: String) = _uiState.update { it.copy(formModel = v, formError = null) }
    fun onPlateChange(v: String) = _uiState.update { it.copy(formPlate = v.uppercase(), formError = null) }
    fun onColorChange(v: String) = _uiState.update { it.copy(formColor = v) }
    fun onYearChange(v: String) = _uiState.update { it.copy(formYear = v.filter { c -> c.isDigit() }.take(4)) }
    fun onTypeChange(v: VehicleType) = _uiState.update { it.copy(formType = v) }

    fun saveVehicle() {
        val state = _uiState.value
        when {
            state.formBrand.isBlank() -> { _uiState.update { it.copy(formError = "Merek kendaraan wajib diisi") }; return }
            state.formModel.isBlank() -> { _uiState.update { it.copy(formError = "Model kendaraan wajib diisi") }; return }
            state.formPlate.isBlank() -> { _uiState.update { it.copy(formError = "Nomor polisi wajib diisi") }; return }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val driverId = userRepository.getCurrentUserUuid()
            val vehicle = VehicleInfo(
                brand = state.formBrand.trim(),
                model = state.formModel.trim(),
                plate = state.formPlate.trim(),
                type = state.formType,
                color = state.formColor.trim().ifBlank { null },
                year = state.formYear.toIntOrNull(),
                isVerified = true
            )
            vehicleRepository.addVehicle(driverId, vehicle)
                .onSuccess { saved ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isAddSheetOpen = false,
                            vehicles = it.vehicles + saved,
                            message = "Kendaraan berhasil ditambahkan"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            formError = e.message ?: "Gagal menyimpan kendaraan"
                        )
                    }
                }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            vehicleRepository.deleteVehicle(vehicleId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            vehicles = it.vehicles.filter { v -> v.id != vehicleId },
                            message = "Kendaraan dihapus"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(message = e.message ?: "Gagal menghapus kendaraan") }
                }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
