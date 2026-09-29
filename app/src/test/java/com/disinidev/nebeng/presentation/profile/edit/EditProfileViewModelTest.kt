package com.disinidev.nebeng.presentation.profile.edit

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
class EditProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var viewModel: EditProfileViewModel

    @Before
    fun setUp() {
        coEvery { userRepository.getCurrentUserUuid() } returns "user_123"
        coEvery { userRepository.getUserProfile() } returns Result.success(
            UserProfileData(
                id = "user_123",
                fullName = "Budi Santoso",
                phoneNumber = "+62 812-3456-7890",
                email = "budi.santoso@email.com",
                officeAddress = "PT Telkom Indonesia • SCBD Lot 8",
                bio = "Komuter santai"
            )
        )
        coEvery { userRepository.uploadAvatar(any(), any()) } returns Result.success("https://supabase.co/avatar.jpg")
        coEvery { userRepository.updateUserProfile(any(), any(), any()) } returns Result.success(Unit)

        viewModel = EditProfileViewModel(userRepository)
    }

    @Test
    fun `initial state has default Budi Santoso profile details`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("Budi Santoso", state.fullName)
        assertEquals("+62 812-3456-7890", state.whatsappNumber)
        assertEquals("budi.santoso@email.com", state.email)
        assertEquals("PT Telkom Indonesia • SCBD Lot 8", state.officeBuilding)
        assertEquals("BS", state.avatarInitials)
        assertFalse(state.isSaving)
    }

    @Test
    fun `onFullNameChange updates fullName and initials`() {
        viewModel.onFullNameChange("Ahmad Fauzi")

        val state = viewModel.uiState.value
        assertEquals("Ahmad Fauzi", state.fullName)
        assertEquals("AF", state.avatarInitials)
    }

    @Test
    fun `onWhatsappChange updates whatsappNumber`() {
        viewModel.onWhatsappChange("+62 811-9988-7766")
        assertEquals("+62 811-9988-7766", viewModel.uiState.value.whatsappNumber)
    }

    @Test
    fun `onEmailChange updates email`() {
        viewModel.onEmailChange("new.email@test.com")
        assertEquals("new.email@test.com", viewModel.uiState.value.email)
    }

    @Test
    fun `onOfficeChange updates officeBuilding`() {
        viewModel.onOfficeChange("Menara Astra • Sudirman")
        assertEquals("Menara Astra • Sudirman", viewModel.uiState.value.officeBuilding)
    }

    @Test
    fun `onBioChange updates bio`() {
        viewModel.onBioChange("Bio baru komuter")
        assertEquals("Bio baru komuter", viewModel.uiState.value.bio)
    }

    @Test
    fun `saveProfile completes and invokes callback`() = runTest {
        var callbackCalled = false
        viewModel.saveProfile {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertTrue(callbackCalled)
        assertFalse(viewModel.uiState.value.isSaving)
    }
}
