package com.disinidev.nebeng.presentation.profile.edit

import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.SupabaseClient
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

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private lateinit var viewModel: EditProfileViewModel

    @Before
    fun setUp() {
        viewModel = EditProfileViewModel(firebaseAuth, supabaseClient)
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
