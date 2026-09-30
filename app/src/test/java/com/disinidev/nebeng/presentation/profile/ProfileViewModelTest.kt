package com.disinidev.nebeng.presentation.profile

import com.disinidev.nebeng.domain.model.VehicleInfo
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.UserProfileData
import com.disinidev.nebeng.domain.repository.UserRepository
import com.disinidev.nebeng.domain.repository.VehicleRepository
import com.disinidev.nebeng.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val vehicleRepository = mockk<VehicleRepository>(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    private val testVehicle = VehicleInfo(
        id = "v_1",
        brand = "Toyota",
        model = "Avanza",
        plate = "B 1234 ABC",
        type = VehicleType.CAR,
        color = "Silver",
        year = 2021,
        isVerified = true
    )

    @Before
    fun setUp() {
        coEvery { userRepository.getCurrentUserUuid() } returns "user_123"
        coEvery { userRepository.getUserProfile() } returns Result.success(
            UserProfileData(
                id = "user_123",
                fullName = "Budi Santoso",
                phoneNumber = "+62 812-3456-7890",
                email = "budi.santoso@email.com",
                rating = 4.9f,
                totalTrips = 15
            )
        )
        coEvery { userRepository.logout() } returns Result.success(Unit)
        coEvery { userRepository.updateUserRole(any()) } returns Result.success(Unit)
        coEvery { userRepository.getUserRoleStats("driver") } returns Result.success(Pair(8, 68))
        coEvery { userRepository.getUserRoleStats("passenger") } returns Result.success(Pair(15, 42))
        coEvery { vehicleRepository.getDriverVehicles("user_123") } returns Result.success(listOf(testVehicle))
        coEvery { vehicleRepository.addVehicle(any(), any()) } returns Result.success(testVehicle)
        coEvery { vehicleRepository.deleteVehicle(any()) } returns Result.success(Unit)

        viewModel = ProfileViewModel(userRepository, vehicleRepository)
    }

    @Test
    fun `initial state defaults to passenger with Budi Santoso profile and loads vehicles`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value

        assertEquals("Budi Santoso", state.fullName)
        assertEquals("+62 812-3456-7890", state.phoneNumber)
        assertEquals("budi.santoso@email.com", state.email)
        assertEquals("BS", state.avatarInitials)
        assertEquals(ProfileRole.PASSENGER, state.selectedRole)
        assertEquals(4.9f, state.rating, 0.01f)
        assertEquals(15, state.tripCount)
        assertEquals(42, state.co2SavedKg)
        assertEquals(1, state.vehicles.size)
        assertEquals("Toyota", state.vehicles.first().brand)
    }

    @Test
    fun `toggleRole to DRIVER updates role and driver stats`() = runTest {
        viewModel.toggleRole(ProfileRole.DRIVER)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ProfileRole.DRIVER, state.selectedRole)
        assertEquals(5.0f, state.rating, 0.01f)
        assertEquals(8, state.tripCount)
        assertEquals(68, state.co2SavedKg)
    }

    @Test
    fun `toggleRole back to PASSENGER restores passenger stats`() = runTest {
        viewModel.toggleRole(ProfileRole.DRIVER)
        advanceUntilIdle()
        viewModel.toggleRole(ProfileRole.PASSENGER)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ProfileRole.PASSENGER, state.selectedRole)
        assertEquals(4.9f, state.rating, 0.01f)
        assertEquals(15, state.tripCount)
        assertEquals(42, state.co2SavedKg)
    }

    @Test
    fun `showMessage and clearMessage manage snackbar message`() {
        viewModel.showMessage("Tes pesan")
        assertEquals("Tes pesan", viewModel.uiState.value.message)

        viewModel.clearMessage()
        assertNull(viewModel.uiState.value.message)
    }

    @Test
    fun `deleteVehicle removes vehicle from list`() = runTest {
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.vehicles.size)

        viewModel.deleteVehicle("v_1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.vehicles.isEmpty())
        assertEquals("Kendaraan berhasil dihapus", viewModel.uiState.value.message)
    }

    @Test
    fun `logout signs out and triggers onSuccess`() = runTest {
        var loggedOut = false
        viewModel.logout { loggedOut = true }
        advanceUntilIdle()

        assertTrue(loggedOut)
    }
}
