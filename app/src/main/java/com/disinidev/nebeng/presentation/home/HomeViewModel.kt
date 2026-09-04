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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class HomeUiState(
    val userGreeting: String = "Budi",
    val userLocation: String = "Tebet, Jakarta Selatan",
    val userLat: Double = -6.2297,
    val userLng: Double = 106.8580,
    val userAvatarInitials: String = "BS",
    val avatarUrl: String? = null,
    val selectedService: ServiceType = ServiceType.CAR,
    val popularRides: List<Ride> = emptyList(),
    val totalRidesCount: Int = 14,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@Serializable
private data class UserProfileDto(
    val id: String,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val office_address: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val supabaseClient: SupabaseClient,
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        syncFcmToken()
        loadPopularRides()
    }

    fun onServiceSelected(service: ServiceType) {
        _uiState.update { it.copy(selectedService = service) }
    }

    fun refreshRides() {
        fetchCurrentLocation()
        loadPopularRides(isRefresh = true)
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
                val token = firebaseMessaging.token.await()
                val uid = firebaseAuth.currentUser?.uid
                if (uid != null && token.isNotBlank()) {
                    supabaseClient.postgrest["users"].update(
                        mapOf("fcm_token" to token)
                    ) {
                        filter {
                            eq("firebase_uid", uid)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Offline fallback
            }
        }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            viewModelScope.launch {
                try {
                    val profile = supabaseClient.postgrest["users"]
                        .select {
                            filter {
                                eq("firebase_uid", uid)
                            }
                        }
                        .decodeSingleOrNull<UserProfileDto>()

                    if (profile != null && !profile.full_name.isNullOrBlank()) {
                        val fullName = profile.full_name
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
                                avatarUrl = profile.avatar_url,
                                userLocation = profile.office_address ?: it.userLocation
                            )
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Fallback to default Budi Santoso
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

            try {
                // Query nearby popular rides
                val fallbackRides = getCuratedPopularRides()
                _uiState.update {
                    it.copy(
                        popularRides = fallbackRides,
                        totalRidesCount = 14,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        popularRides = getCuratedPopularRides(),
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.localizedMessage
                    )
                }
            }
        }
    }

    private fun getCuratedPopularRides(): List<Ride> {
        val zone = ZoneId.of("Asia/Jakarta")
        val today = LocalDate.now(zone)

        val time730 = today.atTime(LocalTime.of(7, 30)).atZone(zone).toInstant()
        val time745 = today.atTime(LocalTime.of(7, 45)).atZone(zone).toInstant()
        val time800 = today.atTime(LocalTime.of(8, 0)).atZone(zone).toInstant()

        return listOf(
            Ride(
                id = "ride_1",
                driverId = "driver_1",
                driver = User(
                    id = "driver_1",
                    firebaseUid = "uid_andi",
                    fullName = "Andi Pratama",
                    phoneNumber = "+628123456701",
                    averageRating = 4.9f,
                    totalTrips = 120,
                    role = UserRole.DRIVER
                ),
                vehicleInfo = VehicleInfo(
                    id = "veh_1",
                    brand = "Toyota",
                    model = "Avanza",
                    plate = "B 1234 ABC",
                    type = VehicleType.CAR
                ),
                maxPassengers = 4,
                availableSeats = 2,
                pickupAddress = "Stasiun Tebet",
                pickupLat = -6.2297,
                pickupLng = 106.8580,
                dropoffAddress = "SCBD Sudirman Lot 8",
                dropoffLat = -6.2274,
                dropoffLng = 106.8080,
                departureTime = time730,
                status = RideStatus.AVAILABLE
            ),
            Ride(
                id = "ride_2",
                driverId = "driver_2",
                driver = User(
                    id = "driver_2",
                    firebaseUid = "uid_dimas",
                    fullName = "Dimas Setiawan",
                    phoneNumber = "+628123456702",
                    averageRating = 4.8f,
                    totalTrips = 95,
                    role = UserRole.DRIVER
                ),
                vehicleInfo = VehicleInfo(
                    id = "veh_2",
                    brand = "Honda",
                    model = "HR-V",
                    plate = "B 5678 DEF",
                    type = VehicleType.CAR
                ),
                maxPassengers = 4,
                availableSeats = 3,
                pickupAddress = "Tebet Eco Park",
                pickupLat = -6.2372,
                pickupLng = 106.8533,
                dropoffAddress = "Mega Kuningan",
                dropoffLat = -6.2289,
                dropoffLng = 106.8268,
                departureTime = time745,
                status = RideStatus.AVAILABLE
            ),
            Ride(
                id = "ride_3",
                driverId = "driver_3",
                driver = User(
                    id = "driver_3",
                    firebaseUid = "uid_rian",
                    fullName = "Rian Firmansyah",
                    phoneNumber = "+628123456703",
                    averageRating = 4.9f,
                    totalTrips = 210,
                    role = UserRole.DRIVER
                ),
                vehicleInfo = VehicleInfo(
                    id = "veh_3",
                    brand = "Yamaha",
                    model = "NMAX 155",
                    plate = "B 9012 GHI",
                    type = VehicleType.MOTORCYCLE
                ),
                maxPassengers = 1,
                availableSeats = 1,
                pickupAddress = "Stasiun Cawang",
                pickupLat = -6.2425,
                pickupLng = 106.8631,
                dropoffAddress = "Menara Mandiri",
                dropoffLat = -6.2255,
                dropoffLng = 106.8078,
                departureTime = time800,
                status = RideStatus.AVAILABLE
            )
        )
    }
}
