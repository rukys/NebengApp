package com.disinidev.nebeng.presentation.profile

import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        viewModel = ProfileViewModel(firebaseAuth, supabaseClient)
    }

    @Test
    fun `initial state defaults to passenger with Budi Santoso profile`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("Budi Santoso", state.fullName)
        assertEquals("+62 812-3456-7890", state.phoneNumber)
        assertEquals("budi.santoso@email.com", state.email)
        assertEquals("B", state.avatarInitials)
        assertEquals(ProfileRole.PASSENGER, state.selectedRole)
        assertEquals(4.9f, state.rating, 0.01f)
        assertEquals(15, state.tripCount)
        assertEquals(42, state.co2SavedKg)
    }

    @Test
    fun `toggleRole to DRIVER updates role and driver stats`() = runTest {
        viewModel.toggleRole(ProfileRole.DRIVER)

        val state = viewModel.uiState.value
        assertEquals(ProfileRole.DRIVER, state.selectedRole)
        assertEquals(5.0f, state.rating, 0.01f)
        assertEquals(8, state.tripCount)
        assertEquals(68, state.co2SavedKg)
    }

    @Test
    fun `toggleRole back to PASSENGER restores passenger stats`() = runTest {
        viewModel.toggleRole(ProfileRole.DRIVER)
        viewModel.toggleRole(ProfileRole.PASSENGER)

        val state = viewModel.uiState.value
        assertEquals(ProfileRole.PASSENGER, state.selectedRole)
        assertEquals(4.9f, state.rating, 0.01f)
        assertEquals(15, state.tripCount)
        assertEquals(42, state.co2SavedKg)
    }

    @Test
    fun `showMessage and clearMessage manage snackbar message`() {
        viewModel.showMessage("Tes pesan")
        assertEquals("Tes pesan", viewModel.uiState.value.message)

        viewModel.clearMessage()
        assertNull(viewModel.uiState.value.message)
    }
}
