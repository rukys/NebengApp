package com.disinidev.nebeng.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.core.component.ServiceType
import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.domain.model.Ride
import com.disinidev.nebeng.domain.model.RideStatus
import com.disinidev.nebeng.domain.model.User
import com.disinidev.nebeng.domain.model.UserRole
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.NotificationRepository
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class HomeUiState(
    val userGreeting: String = "Teman",
    val userLocation: String = "Tebet, Jakarta Selatan",
    val userLat: Double = -6.2297,
    val userLng: Double = 106.8580,
    val userAvatarInitials: String = "",
    val avatarUrl: String? = null,
    val unreadNotificationCount: Int = 0,
    val selectedService: ServiceType = ServiceType.CAR,
    val popularRides: List<Ride> = emptyList(),
    val totalRidesCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val userRepository: UserRepository,
    private val locationClient: LocationClient,
    private val notificationRepository: NotificationRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        syncFcmToken()
        loadPopularRides()
        loadUnreadNotificationCount()
    }

    fun onServiceSelected(service: ServiceType) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun refreshRides() {
        fetchCurrentLocation()
        loadPopularRides(isRefresh = true)
        loadUnreadNotificationCount()
    }

    fun loadUnreadNotificationCount() {
        viewModelScope.launch {
            try {
                val count = notificationRepository?.getUnreadCount()?.getOrNull() ?: 0
                _uiState.update { it.copy(unreadNotificationCount = count) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep current state
            }
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            try {
                val location = locationClient.getCurrentLocation()
                if (location != null) {
                    val address = location.addressName ?: "Lokasi Saat Ini"
                    _uiState.update {
                        it.copy(
                            userLocation = address,
                            userLat = location.latitude,
                            userLng = location.longitude
                        )
                    }
                }
            } catch (_: Exception) {
                // Keep default Tebet
            }
        }
    }

    private fun syncFcmToken() {
        viewModelScope.launch {
            try {
                userRepository.syncFcmToken()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile()
                .onSuccess { profile ->
                    val fullName = profile.fullName
                    val firstName = fullName.split(" ").firstOrNull() ?: fullName
                    val initials = fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                        .uppercase()

                    _uiState.update {
                        it.copy(
                            userGreeting = firstName,
                            userAvatarInitials = initials,
                            avatarUrl = profile.avatarUrl,
                            userLocation = profile.officeAddress ?: it.userLocation
                        )
                    }
                }
        }
    }

    private fun loadPopularRides(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            rideRepository.getPopularRides()
                .onSuccess { remoteList ->
                    _uiState.update {
                        it.copy(
                            popularRides = remoteList,
                            totalRidesCount = remoteList.size,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            popularRides = emptyList(),
                            totalRidesCount = 0,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }
}
