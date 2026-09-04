package com.disinidev.nebeng.presentation.auth.otp

import androidx.lifecycle.SavedStateHandle
import com.disinidev.nebeng.util.MainDispatcherRule
import com.google.firebase.auth.FirebaseAuth
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OtpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: OtpViewModel

    @Before
    fun setUp() {
        savedStateHandle = SavedStateHandle(mapOf("phoneNumber" to "+62 812-3456-7890"))
        viewModel = OtpViewModel(savedStateHandle, firebaseAuth)
    }

    @Test
    fun `initial state extracts phone number from savedStateHandle`() {
        assertEquals("+62 812-3456-7890", viewModel.uiState.value.phoneNumber)
        assertEquals("", viewModel.uiState.value.otpCode)
        assertFalse(viewModel.uiState.value.canResend)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `setPhoneNumber updates phone number state`() {
        viewModel.setPhoneNumber("+62 899-9999-0000")
        assertEquals("+62 899-9999-0000", viewModel.uiState.value.phoneNumber)
    }

    @Test
    fun `onOtpChange updates otp code and clears error`() {
        viewModel.onOtpChange("123")
        assertEquals("123", viewModel.uiState.value.otpCode)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `verifyOtp with less than 6 digits sets error`() {
        viewModel.onOtpChange("123")
        viewModel.verifyOtp()

        assertEquals("Masukkan 6 digit kode keamanan", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `auto submits when 6 digits are typed and verifies successfully`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onOtpChange("123456")

        testScheduler.advanceTimeBy(1500)
        testScheduler.runCurrent()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun `countdown timer ticks down and enables resend after 60 seconds`() = runTest(mainDispatcherRule.testDispatcher) {
        testScheduler.advanceTimeBy(60_000)
        testScheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(0, state.countdownSeconds)
        assertTrue(state.canResend)
    }
}
