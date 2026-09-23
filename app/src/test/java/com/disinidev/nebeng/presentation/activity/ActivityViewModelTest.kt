package com.disinidev.nebeng.presentation.activity

import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private lateinit var viewModel: ActivityViewModel

    @Before
    fun setUp() {
        viewModel = ActivityViewModel(firebaseAuth, supabaseClient)
    }

    @Test
    fun `initial state contains ongoing filter, active trip, and completed history`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals(ActivityFilter.ONGOING, state.selectedFilter)
        assertNotNull(state.activeTrip)
        assertEquals("489 201", state.activeTrip?.pin)
        assertEquals("Andi Pratama", state.activeTrip?.driverName)
        assertTrue(state.completedTrips.isNotEmpty())
        assertEquals("Pancoran", state.completedTrips.first().origin)
        assertEquals("Kuningan", state.completedTrips.first().destination)
        assertEquals("Kemarin, 08:30", state.completedTrips.first().timeText)
        assertEquals("Mobil", state.completedTrips.first().vehicleType)
    }

    @Test
    fun `onFilterSelected updates selectedFilter`() {
        viewModel.onFilterSelected(ActivityFilter.COMPLETED)
        assertEquals(ActivityFilter.COMPLETED, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(ActivityFilter.CANCELED)
        assertEquals(ActivityFilter.CANCELED, viewModel.uiState.value.selectedFilter)

        viewModel.onFilterSelected(ActivityFilter.ONGOING)
        assertEquals(ActivityFilter.ONGOING, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun `onSearchQueryChange updates search query in state`() {
        viewModel.onSearchQueryChange("Pancoran")
        assertEquals("Pancoran", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `toggleSearch toggles search state and resets query when deactivated`() {
        viewModel.toggleSearch(true)
        assertTrue(viewModel.uiState.value.isSearchActive)

        viewModel.onSearchQueryChange("Tebet")
        assertEquals("Tebet", viewModel.uiState.value.searchQuery)

        viewModel.toggleSearch(false)
        assertFalse(viewModel.uiState.value.isSearchActive)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }
}
