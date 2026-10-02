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
import com.disinidev.nebeng.domain.repository.BookingRepository
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
    val popularDestinations: List<String> = listOf("SCBD", "Sudirman", "Kuningan", "Stasiun Manggarai", "Blok M", "Senayan"),
    val totalRidesCount: Int = 0,
    val pendingDriverRequestsCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val userRepository: UserRepository,
    private val locationClient: LocationClient,
    private val bookingRepository: BookingRepository,
    private val notificationRepository: NotificationRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        syncFcmToken()
        loadPopularRides()
        loadUnreadNotificationCount()
        loadPendingDriverRequests()
        fetchCurrentLocation()
    }

    fun onServiceSelected(service: ServiceType) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun refreshRides() {
        fetchCurrentLocation()
        loadPopularRides(isRefresh = true)
        loadUnreadNotificationCount()
        loadPendingDriverRequests()
    }

    fun loadPendingDriverRequests() {
        viewModelScope.launch {
            try {
                val userUuid = userRepository.getCurrentUserUuid()
                val count = bookingRepository.getPendingRequests(userUuid).getOrNull()?.size ?: 0
                _uiState.update { it.copy(pendingDriverRequestsCount = count) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep current state
            }
        }
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
                    _uiState.update { current ->
                        val dynamicDestinations = resolveDynamicDestinations(
                            rides = current.popularRides,
                            lat = location.latitude,
                            lng = location.longitude,
                            locationName = address
                        )
                        current.copy(
                            userLocation = address,
                            userLat = location.latitude,
                            userLng = location.longitude,
                            popularDestinations = dynamicDestinations
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
                            avatarUrl = profile.avatarUrl
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
                    _uiState.update { current ->
                        val dynamicDestinations = resolveDynamicDestinations(
                            rides = remoteList,
                            lat = current.userLat,
                            lng = current.userLng,
                            locationName = current.userLocation
                        )
                        current.copy(
                            popularRides = remoteList,
                            popularDestinations = dynamicDestinations,
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

    private fun resolveDynamicDestinations(
        rides: List<Ride>,
        lat: Double,
        lng: Double,
        locationName: String
    ): List<String> {
        val extractedFromRides = rides
            .map { it.dropoffAddress.trim() }
            .filter { it.isNotBlank() }
            .map { cleanDestinationName(it) }
            .filter { it.isNotBlank() && !it.equals(locationName, ignoreCase = true) }
            .distinct()

        val geoHubs = getGeoHubsForLocation(lat, lng, locationName)

        val combined = (extractedFromRides + geoHubs).distinct().take(6)
        return combined.ifEmpty {
            listOf("SCBD", "Sudirman", "Kuningan", "Stasiun MRT", "Stasiun KRL")
        }
    }

    private fun cleanDestinationName(address: String): String {
        val keywords = listOf(
            "SCBD", "Sudirman", "Kuningan", "Senayan", "Blok M",
            "Thamrin", "Monas", "Manggarai", "Dukuh Atas", "Cawang",
            "Gatot Subroto", "TB Simatupang", "Cilandak", "Kemang",
            "Pancoran", "Slipi", "Tomang", "Kelapa Gading", "Sunter"
        )
        for (kw in keywords) {
            if (address.contains(kw, ignoreCase = true)) {
                return kw
            }
        }
        val firstPart = address.split(",").firstOrNull()?.trim() ?: address
        return if (firstPart.length > 20) firstPart.take(20).trimEnd() + "..." else firstPart
    }

    private fun getGeoHubsForLocation(lat: Double, lng: Double, locationName: String): List<String> {
        val lowerLocation = locationName.lowercase()
        return when {
            lowerLocation.contains("depok") || lowerLocation.contains("bogor") || lowerLocation.contains("cibubur") || lat < -6.32 -> {
                listOf("TB Simatupang", "Cilandak", "Kuningan", "Stasiun UI", "Pancoran", "SCBD")
            }
            lowerLocation.contains("tangerang") || lowerLocation.contains("bsd") || lowerLocation.contains("serpong") || lowerLocation.contains("bintaro") || lng < 106.75 -> {
                listOf("Slipi", "Senayan", "Gading Serpong", "Stasiun Jurangmangu", "Sudirman")
            }
            lowerLocation.contains("bekasi") || lowerLocation.contains("cikarang") || lowerLocation.contains("cawang") || lng > 106.89 -> {
                listOf("MT Haryono", "Cawang", "Gatot Subroto", "Kelapa Gading", "Kuningan")
            }
            lowerLocation.contains("jakarta pusat") || lowerLocation.contains("jakarta utara") || (lat > -6.20 && lat < -6.10) -> {
                listOf("Monas", "Thamrin", "Sudirman", "Kelapa Gading", "Stasiun Gambir")
            }
            else -> {
                listOf("SCBD", "Sudirman", "Kuningan", "Stasiun Manggarai", "Blok M", "Senayan")
            }
        }
    }
}
