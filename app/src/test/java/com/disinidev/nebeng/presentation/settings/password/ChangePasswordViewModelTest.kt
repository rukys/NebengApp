package com.disinidev.nebeng.presentation.settings.password

import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
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
class ChangePasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setUp() {
        every { firebaseAuth.currentUser } returns null
        viewModel = ChangePasswordViewModel(firebaseAuth)
    }

    @Test
    fun `initial state has empty fields and hidden passwords`() {
        val state = viewModel.uiState.value
        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isCurrentPasswordVisible)
        assertFalse(state.isNewPasswordVisible)
        assertFalse(state.isConfirmPasswordVisible)
        assertFalse(state.isLoading)
    }

    @Test
    fun `password text changes update state and reset error`() {
        viewModel.onCurrentPasswordChange("oldPass123")
        viewModel.onNewPasswordChange("newPass123")
        viewModel.onConfirmPasswordChange("newPass123")

        val state = viewModel.uiState.value
        assertEquals("oldPass123", state.currentPassword)
        assertEquals("newPass123", state.newPassword)
        assertEquals("newPass123", state.confirmPassword)
    }

    @Test
    fun `password visibility toggles change state`() {
        viewModel.toggleCurrentPasswordVisibility()
        assertTrue(viewModel.uiState.value.isCurrentPasswordVisible)

        viewModel.toggleNewPasswordVisibility()
        assertTrue(viewModel.uiState.value.isNewPasswordVisible)

        viewModel.toggleConfirmPasswordVisibility()
        assertTrue(viewModel.uiState.value.isConfirmPasswordVisible)
    }

    @Test
    fun `submitChangePassword fails when new password shorter than 6 chars`() {
        viewModel.onNewPasswordChange("12345")
        viewModel.onConfirmPasswordChange("12345")

        var successCalled = false
        viewModel.submitChangePassword { successCalled = true }

        assertFalse(successCalled)
        assertEquals("Kata sandi baru minimal 6 karakter", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitChangePassword fails when confirm password does not match`() {
        viewModel.onNewPasswordChange("newPassword1")
        viewModel.onConfirmPasswordChange("newPassword2")

        var successCalled = false
        viewModel.submitChangePassword { successCalled = true }

        assertFalse(successCalled)
        assertEquals("Konfirmasi kata sandi tidak cocok", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitChangePassword succeeds when validation passes`() = runTest {
        viewModel.onNewPasswordChange("correctPass123")
        viewModel.onConfirmPasswordChange("correctPass123")

        var successCalled = false
        viewModel.submitChangePassword { successCalled = true }
        advanceUntilIdle()

        assertTrue(successCalled)
        assertTrue(viewModel.uiState.value.isSuccess)
    }
}
