package com.disinidev.nebeng.presentation.home

import com.disinidev.nebeng.core.component.ServiceType
import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.core.location.UserLocation
import com.disinidev.nebeng.domain.model.Ride
import com.disinidev.nebeng.domain.model.User
import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.domain.repository.UserProfileData
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val rideRepository = mockk<RideRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val locationClient = mockk<LocationClient>(relaxed = true)
    private lateinit var viewModel: HomeViewModel

    private val sampleRide = Ride(
        id = "ride_1",
        driverId = "d_1",
        driver = User(
            id = "d_1",
            firebaseUid = "fb_1",
            fullName = "Andi Pratama",
            phoneNumber = "08123456789",
            avatarUrl = null
        ),
        vehicleInfo = VehicleInfo(
            id = "v_1",
            brand = "Toyota",
            model = "Avanza Silver",
            plate = "B 1234 ABC",
            type = VehicleType.CAR
        ),
        maxPassengers = 3,
        availableSeats = 2,
        pickupAddress = "Stasiun Tebet",
        pickupLat = -6.2297,
        pickupLng = 106.8580,
        dropoffAddress = "SCBD Sudirman Lot 8",
        dropoffLat = -6.2250,
        dropoffLng = 106.8097,
        departureTime = Instant.now()
    )

    @Before
    fun setUp() {
        coEvery { locationClient.getCurrentLocation() } returns null
        coEvery { userRepository.getCurrentUserName() } returns "Budi Santoso"
        coEvery { userRepository.getUserProfile() } returns Result.success(
            UserProfileData(
                id = "user_123",
                fullName = "Budi Santoso",
                phoneNumber = "+62 812-3456-7890",
                email = "budi.santoso@email.com"
            )
        )
        coEvery { rideRepository.getPopularRides() } returns Result.success(listOf(sampleRide))

        viewModel = HomeViewModel(rideRepository, userRepository, locationClient)
    }

    @Test
    fun `initial state contains default user greeting and location`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Budi", state.userGreeting)
        assertEquals("Tebet, Jakarta Selatan", state.userLocation)
        assertEquals("BS", state.userAvatarInitials)
        assertEquals(ServiceType.CAR, state.selectedService)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initial state loads popular rides list`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.popularRides.isNotEmpty())
        assertEquals("Andi Pratama", state.popularRides.first().driver?.fullName)
        assertEquals("Stasiun Tebet", state.popularRides.first().pickupAddress)
        assertEquals("SCBD Sudirman Lot 8", state.popularRides.first().dropoffAddress)
    }

    @Test
    fun `onServiceSelected updates selected service state`() {
        viewModel.onServiceSelected(ServiceType.MOTORCYCLE)
        assertEquals(ServiceType.MOTORCYCLE, viewModel.uiState.value.selectedService)

        viewModel.onServiceSelected(ServiceType.OFFER_RIDE)
        assertEquals(ServiceType.OFFER_RIDE, viewModel.uiState.value.selectedService)

        viewModel.onServiceSelected(ServiceType.ROUTINE)
        assertEquals(ServiceType.ROUTINE, viewModel.uiState.value.selectedService)
    }

    @Test
    fun `fetchCurrentLocation updates location address in state`() = runTest {
        coEvery { locationClient.getCurrentLocation() } returns UserLocation(
            latitude = -6.2088,
            longitude = 106.8456,
            addressName = "Menteng, Jakarta Pusat"
        )

        viewModel.fetchCurrentLocation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Menteng, Jakarta Pusat", state.userLocation)
        assertEquals(-6.2088, state.userLat, 0.0001)
        assertEquals(106.8456, state.userLng, 0.0001)
    }

    @Test
    fun `refreshRides refreshes popular rides`() = runTest {
        viewModel.refreshRides()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertTrue(state.popularRides.isNotEmpty())
    }

    @Test
    fun `loadUnreadNotificationCount updates unread count in state`() = runTest {
        val notifRepo = mockk<com.disinidev.nebeng.domain.repository.NotificationRepository>()
        coEvery { notifRepo.getUnreadCount() } returns Result.success(5)
        val vm = HomeViewModel(rideRepository, userRepository, locationClient, notifRepo)
        advanceUntilIdle()

        assertEquals(5, vm.uiState.value.unreadNotificationCount)
    }
}
