package com.disinidev.nebeng.presentation.auth.register

import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.github.jan.supabase.SupabaseClient
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        val authResult = mockk<AuthResult>(relaxed = true)
        val mockUser = mockk<FirebaseUser>(relaxed = true)
        every { mockUser.uid } returns "uid_test_123"
        every { authResult.user } returns mockUser
        every { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns Tasks.forResult(authResult)

        viewModel = RegisterViewModel(firebaseAuth, supabaseClient)
    }

    @Test
    fun `initial state has default empty values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.fullName)
        assertEquals("", state.email)
        assertEquals("", state.phoneNumber)
        assertEquals("", state.password)
        assertFalse(state.termsAgreed)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `onFullNameChange updates full name and clears error`() {
        viewModel.onFullNameChange("Budi Santoso")
        assertEquals("Budi Santoso", viewModel.uiState.value.fullName)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onEmailChange updates email and clears error`() {
        viewModel.onEmailChange("budi@example.com")
        assertEquals("budi@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `register with blank full name shows error`() {
        viewModel.onEmailChange("budi@example.com")
        viewModel.onPhoneNumberChange("081234567890")
        viewModel.onPasswordChange("password123")
        viewModel.onTermsAgreedChange(true)

        viewModel.register()

        assertEquals("Nama lengkap harus diisi", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `register with invalid email shows error`() {
        viewModel.onFullNameChange("Budi Santoso")
        viewModel.onEmailChange("invalid-email")
        viewModel.onPhoneNumberChange("081234567890")
        viewModel.onPasswordChange("password123")
        viewModel.onTermsAgreedChange(true)

        viewModel.register()

        assertEquals("Format email tidak valid", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `register with short phone number shows error`() {
        viewModel.onFullNameChange("Budi Santoso")
        viewModel.onEmailChange("budi@example.com")
        viewModel.onPhoneNumberChange("12345")
        viewModel.onPasswordChange("password123")
        viewModel.onTermsAgreedChange(true)

        viewModel.register()

        assertEquals("Nomor WhatsApp tidak valid", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `register with short password shows error`() {
        viewModel.onFullNameChange("Budi Santoso")
        viewModel.onEmailChange("budi@example.com")
        viewModel.onPhoneNumberChange("081234567890")
        viewModel.onPasswordChange("pass")
        viewModel.onTermsAgreedChange(true)

        viewModel.register()

        assertEquals("Kata sandi minimal 8 karakter", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `register without terms agreement shows error`() {
        viewModel.onFullNameChange("Budi Santoso")
        viewModel.onEmailChange("budi@example.com")
        viewModel.onPhoneNumberChange("081234567890")
        viewModel.onPasswordChange("password123")
        viewModel.onTermsAgreedChange(false)

        viewModel.register()

        assertEquals("Anda harus menyetujui Syarat Layanan & Kebijakan Privasi", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `register with valid data succeeds and formats indonesian phone number`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onFullNameChange("Budi Santoso")
        viewModel.onEmailChange("budi@example.com")
        viewModel.onPhoneNumberChange("081234567890")
        viewModel.onPasswordChange("password123")
        viewModel.onTermsAgreedChange(true)

        viewModel.register()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertEquals("+6281234567890", state.phoneNumber)
    }
}
