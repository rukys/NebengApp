package com.disinidev.nebeng.presentation.home

import com.disinidev.nebeng.core.component.ServiceType
import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.core.location.UserLocation
import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.SupabaseClient
import io.mockk.coEvery
import io.mockk.every
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val firebaseMessaging = mockk<FirebaseMessaging>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private val locationClient = mockk<LocationClient>(relaxed = true)
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        every { firebaseMessaging.token } returns Tasks.forResult("mock_fcm_token_123")
        coEvery { locationClient.getCurrentLocation() } returns null
        viewModel = HomeViewModel(firebaseAuth, firebaseMessaging, supabaseClient, locationClient)
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
}
