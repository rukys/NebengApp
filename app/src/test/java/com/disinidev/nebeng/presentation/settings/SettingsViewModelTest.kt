package com.disinidev.nebeng.presentation.settings

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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        coEvery { userRepository.getUserProfile() } returns Result.success(
            UserProfileData(
                id = "user_123",
                fullName = "Budi Santoso",
                phoneNumber = "+62 812-3456-7890",
                email = "budi.santoso@email.com"
            )
        )
        coEvery { userRepository.logout() } returns Result.success(Unit)
        viewModel = SettingsViewModel(userRepository)
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

        viewModel.logout {
            callbackCalled = true
        }
        advanceUntilIdle()

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
