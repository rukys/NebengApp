package com.disinidev.nebeng.presentation.profile

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
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun toggleRole(role: ProfileRole) {
        _uiState.update { current ->
            current.copy(selectedRole = role)
        }

        viewModelScope.launch {
            try {
                val roleString = if (role == ProfileRole.DRIVER) "driver" else "passenger"
                userRepository.updateUserRole(roleString)
                fetchRoleStats(role)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep current state
            }
        }
    }

    private suspend fun fetchRoleStats(role: ProfileRole) {
        val roleString = if (role == ProfileRole.DRIVER) "driver" else "passenger"
        userRepository.getUserRoleStats(roleString)
            .onSuccess { (count, co2) ->
                val rating = if (role == ProfileRole.DRIVER) 5.0f else 4.9f
                _uiState.update { it.copy(tripCount = count, co2SavedKg = co2, rating = rating) }
            }
            .onFailure {
                // Keep real counts
            }
    }

    fun addVehicle(
        brand: String,
        model: String,
        plate: String,
        type: VehicleType,
        color: String? = null,
        year: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingVehicle = true) }
            try {
                val userUuid = userRepository.getCurrentUserUuid()
                val newVehicle = VehicleInfo(
                    id = "",
                    brand = brand.trim(),
                    model = model.trim(),
                    plate = plate.trim().uppercase(),
                    type = type,
                    color = color?.trim()?.ifBlank { null },
                    year = year,
                    isVerified = false
                )
                vehicleRepository.addVehicle(userUuid, newVehicle)
                    .onSuccess { created ->
                        _uiState.update {
                            it.copy(
                                vehicles = it.vehicles + created,
                                isAddingVehicle = false,
                                message = "Kendaraan berhasil ditambahkan"
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isAddingVehicle = false,
                                message = error.message ?: "Gagal menambahkan kendaraan"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isAddingVehicle = false, message = e.message ?: "Gagal menambahkan kendaraan")
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
                            vehicles = it.vehicles.filterNot { v -> v.id == vehicleId },
                            message = "Kendaraan berhasil dihapus"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(message = error.message ?: "Gagal menghapus kendaraan")
                    }
                }
        }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(message = msg) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun loadVehicles(userUuid: String) {
        viewModelScope.launch {
            vehicleRepository.getDriverVehicles(userUuid)
                .onSuccess { list ->
                    _uiState.update { it.copy(vehicles = list) }
                }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userUuid = userRepository.getCurrentUserUuid()
            loadVehicles(userUuid)

            userRepository.getUserProfile()
                .onSuccess { profile ->
                    val fullName = profile.fullName
                    val initials = fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    val isDriver = profile.role.lowercase() == "driver"

                    _uiState.update { current ->
                        current.copy(
                            fullName = fullName,
                            phoneNumber = profile.phoneNumber,
                            email = profile.email,
                            avatarUrl = profile.avatarUrl,
                            avatarInitials = initials.ifBlank { if (fullName.isNotBlank()) fullName.first().uppercase() else "U" },
                            rating = profile.rating,
                            tripCount = profile.totalTrips,
                            selectedRole = if (isDriver) ProfileRole.DRIVER else ProfileRole.PASSENGER
                        )
                    }

                    fetchRoleStats(if (isDriver) ProfileRole.DRIVER else ProfileRole.PASSENGER)
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.logout()
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Gagal keluar: ${e.message}") }
            }
        }
    }
}
