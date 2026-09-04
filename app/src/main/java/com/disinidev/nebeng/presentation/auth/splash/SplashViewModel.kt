package com.disinidev.nebeng.presentation.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToHome : SplashUiState
    data object NavigateToOnboarding : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // Tampilkan splash screen 2 detik sesuai UX pattern
            delay(2000)
            if (firebaseAuth.currentUser != null) {
                _uiState.value = SplashUiState.NavigateToHome
            } else {
                _uiState.value = SplashUiState.NavigateToOnboarding
            }
        }
    }
}
