package com.disinidev.nebeng.presentation.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.domain.model.RoutineCommute
import com.disinidev.nebeng.domain.repository.RoutineRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoutineCommuteViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineCommuteUiState(isLoading = true))
    val uiState: StateFlow<RoutineCommuteUiState> = _uiState.asStateFlow()

    init {
        loadRoutines()
    }

    fun loadRoutines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = userRepository.getCurrentUserUuid()
            val result = routineRepository.getRoutineCommutes(userId)
            result.fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(isLoading = false, routines = list) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun showAddSheet(show: Boolean) {
        _uiState.update {
            it.copy(
                isAddSheetVisible = show,
                originInput = if (show) it.originInput else "",
                destinationInput = if (show) it.destinationInput else ""
            )
        }
    }

    fun updateOrigin(value: String) {
        _uiState.update { it.copy(originInput = value) }
    }

    fun updateDestination(value: String) {
        _uiState.update { it.copy(destinationInput = value) }
    }

    fun updateDepartureTime(value: String) {
        _uiState.update { it.copy(departureTimeInput = value) }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val updatedDays = if (state.selectedDays.contains(day)) {
                if (state.selectedDays.size > 1) state.selectedDays - day else state.selectedDays
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = updatedDays)
        }
    }

    fun updateVehicleType(type: String) {
        _uiState.update { it.copy(selectedVehicleType = type) }
    }

    fun toggleAutoBook(enabled: Boolean) {
        _uiState.update { it.copy(autoBook = enabled) }
    }

    fun saveRoutine() {
        val currentState = _uiState.value
        val origin = currentState.originInput.trim()
        val destination = currentState.destinationInput.trim()
        if (origin.isBlank() || destination.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val userId = userRepository.getCurrentUserUuid()
            val newRoutine = RoutineCommute(
                id = UUID.randomUUID().toString(),
                userId = userId,
                originName = origin,
                destinationName = destination,
                departureTime = currentState.departureTimeInput.ifBlank { "07:30" },
                activeDays = currentState.selectedDays.sorted(),
                vehicleType = currentState.selectedVehicleType,
                isEnabled = true,
                autoBook = currentState.autoBook
            )

            val result = routineRepository.saveRoutineCommute(newRoutine)
            result.fold(
                onSuccess = { saved ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            isAddSheetVisible = false,
                            originInput = "",
                            destinationInput = "",
                            routines = listOf(saved) + state.routines.filter { it.id != saved.id }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun toggleRoutine(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    routines = state.routines.map {
                        if (it.id == id) it.copy(isEnabled = isEnabled) else it
                    }
                )
            }
            routineRepository.toggleRoutineCommute(id, isEnabled)
        }
    }

    fun deleteRoutine(id: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(routines = state.routines.filter { it.id != id })
            }
            routineRepository.deleteRoutineCommute(id)
        }
    }
}
