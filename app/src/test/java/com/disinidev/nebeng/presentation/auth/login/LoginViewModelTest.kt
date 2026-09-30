package com.disinidev.nebeng.presentation.auth.login

import com.disinidev.nebeng.domain.repository.AuthRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        coEvery { authRepository.loginWithEmail(any(), any()) } returns Result.success(Unit)
        coEvery { authRepository.loginWithGoogle(any()) } returns Result.success(Unit)
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.identifier)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isGoogleLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `onIdentifierChange updates identifier`() {
        viewModel.onIdentifierChange("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.identifier)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onPasswordChange updates password`() {
        viewModel.onPasswordChange("secret123")
        assertEquals("secret123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `login with blank identifier shows error`() {
        viewModel.onPasswordChange("secret123")
        viewModel.login()

        assertEquals("Email atau nomor ponsel tidak boleh kosong", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `login with blank password shows error`() {
        viewModel.onIdentifierChange("test@example.com")
        viewModel.login()

        assertEquals("Kata sandi tidak boleh kosong", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `login with valid credentials succeeds`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onIdentifierChange("test@example.com")
        viewModel.onPasswordChange("secret123")

        viewModel.login()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onGoogleSignInError sets error message and stops loading`() {
        viewModel.setGoogleLoading(true)
        assertTrue(viewModel.uiState.value.isGoogleLoading)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.onGoogleSignInError("Pop-up dibatalkan")
        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertEquals("Pop-up dibatalkan", state.errorMessage)
    }
}
