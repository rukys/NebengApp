package com.disinidev.nebeng.presentation.settings

import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        viewModel = SettingsViewModel(firebaseAuth, supabaseClient)
    }

    @Test
    fun `initial state contains default profile and preferences`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("Budi Santoso", state.fullName)
        assertEquals("budi.santoso@email.com", state.email)
        assertEquals("BS", state.avatarInitials)
        assertTrue(state.isVerified)
        assertTrue(state.isDocumentVerified)
        assertEquals("Bahasa Indonesia", state.selectedLanguage)
        assertTrue(state.isNotificationEnabled)
        assertFalse(state.showLogoutDialog)
    }

    @Test
    fun `showLogoutDialog toggles dialog state`() {
        viewModel.showLogoutDialog(true)
        assertTrue(viewModel.uiState.value.showLogoutDialog)

        viewModel.showLogoutDialog(false)
        assertFalse(viewModel.uiState.value.showLogoutDialog)
    }

    @Test
    fun `logout triggers firebase signOut and invokes callback`() = runTest {
        var callbackCalled = false
        every { firebaseAuth.signOut() } returns Unit

        viewModel.logout {
            callbackCalled = true
        }
        advanceUntilIdle()

        verify { firebaseAuth.signOut() }
        assertTrue(callbackCalled)
        assertFalse(viewModel.uiState.value.showLogoutDialog)
    }

    @Test
    fun `setLanguage updates selectedLanguage`() {
        viewModel.setLanguage("English")
        assertEquals("English", viewModel.uiState.value.selectedLanguage)
    }

    @Test
    fun `toggleNotification updates isNotificationEnabled`() {
        viewModel.toggleNotification(false)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)

        viewModel.toggleNotification(true)
        assertTrue(viewModel.uiState.value.isNotificationEnabled)
    }
}
