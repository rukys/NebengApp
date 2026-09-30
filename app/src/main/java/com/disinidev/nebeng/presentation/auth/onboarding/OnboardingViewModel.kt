package com.disinidev.nebeng.presentation.auth.onboarding

import androidx.lifecycle.ViewModel
import com.disinidev.nebeng.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun completeOnboarding() {
        userPreferencesRepository.setOnboardingCompleted(true)
    }
}
