package com.disinidev.nebeng.presentation.auth.splash

import app.cash.turbine.test
import com.disinidev.nebeng.domain.repository.UserPreferencesRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val userPreferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)

    @Test
    fun `when user is authenticated, navigates to Home`() = runTest {
        val mockUser = mockk<FirebaseUser>()
        every { firebaseAuth.currentUser } returns mockUser

        val viewModel = SplashViewModel(firebaseAuth, userPreferencesRepository)

        viewModel.uiState.test {
            assertEquals(SplashUiState.Loading, awaitItem())
            advanceTimeBy(2001)
            assertEquals(SplashUiState.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun `when user not authenticated but onboarding completed, navigates to Login`() = runTest {
        every { firebaseAuth.currentUser } returns null
        every { userPreferencesRepository.isOnboardingCompleted() } returns true

        val viewModel = SplashViewModel(firebaseAuth, userPreferencesRepository)

        viewModel.uiState.test {
            assertEquals(SplashUiState.Loading, awaitItem())
            advanceTimeBy(2001)
            assertEquals(SplashUiState.NavigateToLogin, awaitItem())
        }
    }

    @Test
    fun `when user not authenticated and onboarding not completed, navigates to Onboarding`() = runTest {
        every { firebaseAuth.currentUser } returns null
        every { userPreferencesRepository.isOnboardingCompleted() } returns false

        val viewModel = SplashViewModel(firebaseAuth, userPreferencesRepository)

        viewModel.uiState.test {
            assertEquals(SplashUiState.Loading, awaitItem())
            advanceTimeBy(2001)
            assertEquals(SplashUiState.NavigateToOnboarding, awaitItem())
        }
    }
}
