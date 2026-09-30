package com.disinidev.nebeng.presentation.auth.onboarding

import com.disinidev.nebeng.domain.repository.UserPreferencesRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class OnboardingViewModelTest {

    private val userPreferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)

    @Test
    fun `completeOnboarding sets onboarding completed to true`() {
        val viewModel = OnboardingViewModel(userPreferencesRepository)

        viewModel.completeOnboarding()

        verify(exactly = 1) { userPreferencesRepository.setOnboardingCompleted(true) }
    }
}
