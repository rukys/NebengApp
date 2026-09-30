package com.disinidev.nebeng.presentation.search.form

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.domain.model.PlaceSuggestion
import com.disinidev.nebeng.domain.repository.LocationSearchRepository
import com.disinidev.nebeng.presentation.search.model.FavoriteLocation
import com.disinidev.nebeng.presentation.search.model.SearchHistoryItem
import com.disinidev.nebeng.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SearchFormViewModel
    private val fakeRepo = object : LocationSearchRepository {
        override suspend fun searchPlaces(query: String): List<PlaceSuggestion> {
            return listOf(
                PlaceSuggestion("p1", "Stasiun Tebet", "Tebet Timur, Jakarta Selatan", -6.2297, 106.8580)
            )
        }
    }

    @Before
    fun setUp() {
        val savedStateHandle = SavedStateHandle(mapOf("vehicleType" to "car"))
        viewModel = SearchFormViewModel(savedStateHandle, fakeRepo)
    }

    @Test
    fun `initial state has correct default values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.origin)
        assertEquals("", state.destination)
        assertNull(state.originLatitude)
        assertNull(state.originLongitude)
        assertNull(state.destinationLatitude)
        assertNull(state.destinationLongitude)
        assertEquals("Hari Ini, 07:30", state.selectedTime)
        assertEquals(1, state.seatCount)
        assertEquals("car", state.vehicleType)
        assertTrue(state.favoriteLocations.isEmpty())
        assertTrue(state.recentSearches.isEmpty())
    }

    @Test
    fun `onOriginChange updates origin text`() {
        viewModel.onOriginChange("Bekasi Barat")
        assertEquals("Bekasi Barat", viewModel.uiState.value.origin)
    }

    @Test
    fun `onDestinationChange updates destination text`() {
        viewModel.onDestinationChange("Kuningan City")
        assertEquals("Kuningan City", viewModel.uiState.value.destination)
    }

    @Test
    fun `onSwapLocations swaps origin and destination`() {
        viewModel.onOriginChange("Tebet")
        viewModel.onDestinationChange("SCBD")
        viewModel.onSwapLocations()

        assertEquals("SCBD", viewModel.uiState.value.origin)
        assertEquals("Tebet", viewModel.uiState.value.destination)
    }

    @Test
    fun `onClearOrigin clears origin text`() {
        viewModel.onClearOrigin()
        assertEquals("", viewModel.uiState.value.origin)
    }

    @Test
    fun `onClearDestination clears destination text`() {
        viewModel.onClearDestination()
        assertEquals("", viewModel.uiState.value.destination)
    }

    @Test
    fun `onSelectFavorite updates destination if origin not empty`() {
        viewModel.onOriginChange("Tebet")
        val favorite = FavoriteLocation("1", "🏢", "Kantor", "SCBD Sudirman")
        viewModel.onSelectFavorite(favorite)
        assertEquals("SCBD Sudirman", viewModel.uiState.value.destination)
    }

    @Test
    fun `onSelectFavorite updates origin if origin is empty`() {
        val favorite = FavoriteLocation("1", "🏢", "Kantor", "SCBD Sudirman")
        viewModel.onSelectFavorite(favorite)
        assertEquals("SCBD Sudirman", viewModel.uiState.value.origin)
    }

    @Test
    fun `onSelectHistory updates both origin and destination`() {
        val history = SearchHistoryItem("1", "Depok Baru", "Cilandak", "06:45 WIB", "8 pilihan")
        viewModel.onSelectHistory(history)
        assertEquals("Depok Baru", viewModel.uiState.value.origin)
        assertEquals("Cilandak", viewModel.uiState.value.destination)
    }

    @Test
    fun `onSeatCountChange updates seat count within range`() {
        viewModel.onSeatCountChange(3)
        assertEquals(3, viewModel.uiState.value.seatCount)

        // Invalid count outside 1..4 should be ignored
        viewModel.onSeatCountChange(5)
        assertEquals(3, viewModel.uiState.value.seatCount)
    }

    @Test
    fun `onVehicleTypeChange updates vehicle type`() {
        viewModel.onVehicleTypeChange("motorcycle")
        assertEquals("motorcycle", viewModel.uiState.value.vehicleType)

        viewModel.onVehicleTypeChange("car")
        assertEquals("car", viewModel.uiState.value.vehicleType)
    }

    @Test
    fun `onTimeChange updates selected time`() {
        viewModel.onTimeChange("Hari Ini, 08:45")
        assertEquals("Hari Ini, 08:45", viewModel.uiState.value.selectedTime)
    }

    @Test
    fun `onSelectSuggestion for ORIGIN sets origin and coordinates`() {
        viewModel.onOriginChange("Tebet")
        val suggestion = PlaceSuggestion("p1", "Stasiun Tebet", "Tebet Timur", -6.2297, 106.8580)
        viewModel.onSelectSuggestion(suggestion)

        val state = viewModel.uiState.value
        assertEquals("Stasiun Tebet", state.origin)
        assertEquals(-6.2297, state.originLatitude ?: 0.0, 0.0001)
        assertEquals(106.8580, state.originLongitude ?: 0.0, 0.0001)
        assertTrue(state.suggestions.isEmpty())
        assertEquals(ActiveSearchField.NONE, state.activeField)
    }

    @Test
    fun `onSelectSuggestion for DESTINATION sets destination and coordinates`() {
        viewModel.onDestinationChange("SCBD")
        val suggestion = PlaceSuggestion("p2", "SCBD Sudirman", "Jakarta Selatan", -6.2274, 106.8080)
        viewModel.onSelectSuggestion(suggestion)

        val state = viewModel.uiState.value
        assertEquals("SCBD Sudirman", state.destination)
        assertEquals(-6.2274, state.destinationLatitude ?: 0.0, 0.0001)
        assertEquals(106.8080, state.destinationLongitude ?: 0.0, 0.0001)
        assertTrue(state.suggestions.isEmpty())
        assertEquals(ActiveSearchField.NONE, state.activeField)
    }

    @Test
    fun `onDismissSuggestions clears suggestions and resets activeField`() {
        viewModel.onOriginChange("Manggarai")
        viewModel.onDismissSuggestions()

        val state = viewModel.uiState.value
        assertTrue(state.suggestions.isEmpty())
        assertEquals(ActiveSearchField.NONE, state.activeField)
    }
}
