package com.disinidev.nebeng.presentation.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.disinidev.nebeng.core.location.LocationClient
import com.disinidev.nebeng.domain.model.TripLocation
import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.domain.repository.BookingRepository
import com.disinidev.nebeng.domain.usecase.ObserveDriverLocationUseCase
import com.disinidev.nebeng.domain.usecase.UpdateDriverLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

import com.disinidev.nebeng.domain.repository.UserRepository

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository,
    private val observeDriverLocationUseCase: ObserveDriverLocationUseCase,
    private val updateDriverLocationUseCase: UpdateDriverLocationUseCase,
    private val locationClient: LocationClient,
    private val userRepository: UserRepository
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: "booking_ride_1"

    private val _uiState = MutableStateFlow(createInitialState(bookingId))
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private var observeJob: Job? = null

    init {
        loadBooking()
        observeDriverLocationStream()
    }

    fun showEmergencyDialog(show: Boolean) {
        _uiState.update { it.copy(isEmergencyDialogOpen = show) }
    }

    /**
     * Listens to live location updates for this booking from Supabase trip_locations table.
     */
    private fun observeDriverLocationStream() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeDriverLocationUseCase(bookingId).collect { tripLoc ->
                if (tripLoc != null) {
                    val stage = _uiState.value.tripStage
                    val targetLat = if (stage == TripStage.IN_TRANSIT) _uiState.value.destinationLat else _uiState.value.pickupLat
                    val targetLng = if (stage == TripStage.IN_TRANSIT) _uiState.value.destinationLng else _uiState.value.pickupLng
                    val dist = calculateDistanceMeters(tripLoc.lat, tripLoc.lng, targetLat, targetLng)
                    // Discard stale remote locations (> 25km away)
                    if (dist <= 25000) {
                        simulationJob?.cancel()
                        onRemoteDriverLocationReceived(tripLoc)
                    }
                }
            }
        }
    }

    private fun onRemoteDriverLocationReceived(tripLoc: TripLocation) {
        val stage = _uiState.value.tripStage
        val targetLat = if (stage == TripStage.IN_TRANSIT) _uiState.value.destinationLat else _uiState.value.pickupLat
        val targetLng = if (stage == TripStage.IN_TRANSIT) _uiState.value.destinationLng else _uiState.value.pickupLng
        val distMeters = calculateDistanceMeters(tripLoc.lat, tripLoc.lng, targetLat, targetLng)
        val etaMin = maxOf(0, (distMeters / 150))
        val isArrived = distMeters <= 25

        _uiState.update { current ->
            val newStage = when {
                stage == TripStage.IN_TRANSIT && isArrived -> TripStage.ARRIVED_DESTINATION
                (stage == TripStage.PICKUP_EN_ROUTE || stage == TripStage.WAITING_FOR_DRIVER) && isArrived -> TripStage.PICKUP_ARRIVED
                stage == TripStage.WAITING_FOR_DRIVER -> TripStage.PICKUP_EN_ROUTE
                else -> current.tripStage
            }
            val status = when (newStage) {
                TripStage.ARRIVED_DESTINATION -> "Tiba di Titik Tujuan!"
                TripStage.IN_TRANSIT -> "Dalam Perjalanan ke Tujuan"
                TripStage.PICKUP_ARRIVED -> "Driver Telah Tiba di Titik Jemput!"
                TripStage.WAITING_FOR_DRIVER -> "Menunggu Konfirmasi Pengemudi..."
                TripStage.PICKUP_EN_ROUTE -> "Driver Sedang Menjemput"
            }
            current.copy(
                driverCurrentLat = tripLoc.lat,
                driverCurrentLng = tripLoc.lng,
                distanceMeters = distMeters,
                etaMinutes = etaMin,
                statusText = status,
                tripStage = newStage,
                isArrived = isArrived,
                progress = if (isArrived) 1.0f else (1.0f - (distMeters / 500f).coerceIn(0f, 1f))
            )
        }
    }

    /**
     * Starts smooth driver simulation approaching user's pickup point.
     */
    fun startLiveTrackingSimulation(
        startLat: Double = _uiState.value.driverCurrentLat,
        startLng: Double = _uiState.value.driverCurrentLng,
        targetLat: Double = _uiState.value.pickupLat,
        targetLng: Double = _uiState.value.pickupLng
    ) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val steps = listOf(
                TrackingStep(progress = 0.0f, distance = 450, eta = 3, status = "Driver Sedang Menjemput", bearing = 45f, speed = 25),
                TrackingStep(progress = 0.25f, distance = 340, eta = 3, status = "Driver Sedang Menjemput", bearing = 48f, speed = 30),
                TrackingStep(progress = 0.55f, distance = 210, eta = 2, status = "Driver Mendekati Titik Jemput", bearing = 40f, speed = 20),
                TrackingStep(progress = 0.82f, distance = 80, eta = 1, status = "Driver Hampir Sampai (80m)", bearing = 45f, speed = 15),
                TrackingStep(progress = 1.0f, distance = 0, eta = 0, status = "Driver Telah Tiba di Titik Jemput!", bearing = 45f, isArrived = true, speed = 0)
            )

            for (step in steps) {
                delay(3500)
                val curLat = startLat + (targetLat - startLat) * step.progress
                val curLng = startLng + (targetLng - startLng) * step.progress

                // Sync live position to Supabase only if current user is the driver
                if (_uiState.value.isCurrentUserDriver) {
                    updateDriverLocationUseCase(bookingId, curLat, curLng)
                }

                val isPickupArrived = step.isArrived
                _uiState.update { current ->
                    current.copy(
                        progress = step.progress,
                        distanceMeters = step.distance,
                        etaMinutes = step.eta,
                        statusText = step.status,
                        driverBearing = step.bearing,
                        driverCurrentLat = curLat,
                        driverCurrentLng = curLng,
                        currentSpeedKmh = step.speed,
                        tripStage = if (isPickupArrived) TripStage.PICKUP_ARRIVED else TripStage.PICKUP_EN_ROUTE,
                        isArrived = step.isArrived
                    )
                }
            }
        }
    }

    /**
     * Starts In-Transit phase (Fase 2: Dalam Perjalanan ke Tujuan).
     */
    fun startInTransitTrip() {
        simulationJob?.cancel()
        val startLat = _uiState.value.pickupLat
        val startLng = _uiState.value.pickupLng
        val destLat = _uiState.value.destinationLat
        val destLng = _uiState.value.destinationLng

        _uiState.update { current ->
            current.copy(
                tripStage = TripStage.IN_TRANSIT,
                statusText = "Dalam Perjalanan ke Tujuan",
                distanceMeters = 2400,
                etaMinutes = 12,
                currentSpeedKmh = 35,
                progress = 0.0f,
                isArrived = false
            )
        }

        simulationJob = viewModelScope.launch {
            val transitSteps = listOf(
                TrackingStep(progress = 0.0f, distance = 2400, eta = 12, status = "Dalam Perjalanan ke Tujuan", bearing = 32f, speed = 35),
                TrackingStep(progress = 0.25f, distance = 1800, eta = 9, status = "Melewati Jalur Bebas Hambatan", bearing = 36f, speed = 48),
                TrackingStep(progress = 0.55f, distance = 1000, eta = 5, status = "Mendekati Area Destinasi", bearing = 28f, speed = 35),
                TrackingStep(progress = 0.85f, distance = 300, eta = 2, status = "Hampir Sampai di Titik Turun", bearing = 22f, speed = 20),
                TrackingStep(progress = 1.0f, distance = 0, eta = 0, status = "Tiba di Titik Tujuan!", bearing = 20f, isArrived = true, speed = 0)
            )

            for (step in transitSteps) {
                delay(3800)
                val curLat = startLat + (destLat - startLat) * step.progress
                val curLng = startLng + (destLng - startLng) * step.progress

                if (_uiState.value.isCurrentUserDriver) {
                    updateDriverLocationUseCase(bookingId, curLat, curLng)
                }

                val isDestinationReached = step.isArrived
                _uiState.update { current ->
                    current.copy(
                        progress = step.progress,
                        distanceMeters = step.distance,
                        etaMinutes = step.eta,
                        statusText = step.status,
                        driverBearing = step.bearing,
                        driverCurrentLat = curLat,
                        driverCurrentLng = curLng,
                        currentSpeedKmh = step.speed,
                        tripStage = if (isDestinationReached) TripStage.ARRIVED_DESTINATION else TripStage.IN_TRANSIT,
                        isArrived = isDestinationReached
                    )
                }
            }
        }
    }

    /**
     * Refreshes user location from device GPS when GPS button is clicked.
     */
    fun recenterToCurrentLocations() {
        viewModelScope.launch {
            if (locationClient.hasLocationPermission()) {
                val loc = locationClient.getCurrentLocation()
                if (loc != null) {
                    _uiState.update { current ->
                        current.copy(
                            pickupLat = loc.latitude,
                            pickupLng = loc.longitude
                        )
                    }
                }
            }
        }
    }

    fun completeTrip(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            bookingRepository.completeTrip(bookingId)
            _uiState.update { it.copy(tripStage = TripStage.ARRIVED_DESTINATION, isArrived = true) }
            onSuccess()
        }
    }

    fun cancelTrip(reason: String? = null, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            bookingRepository.cancelBooking(bookingId, reason)
            onSuccess()
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toInt()
    }

    private data class TrackingStep(
        val progress: Float,
        val distance: Int,
        val eta: Int,
        val status: String,
        val bearing: Float,
        val isArrived: Boolean = false,
        val speed: Int = 0
    )

    private fun loadBooking() {
        viewModelScope.launch {
            val myUuid = userRepository.getCurrentUserUuid()
            val result = bookingRepository.getBookingById(bookingId)
            result.getOrNull()?.let { booking ->
                val isDriver = booking.driverId.isNotBlank() && booking.driverId == myUuid
                val isMotor = booking.vehicleType.equals("motorcycle", ignoreCase = true) ||
                        booking.vehicleType.equals("motor", ignoreCase = true) ||
                        booking.seatPosition == "pillion"

                val isPending = booking.status == "pending"
                val initialStage = when {
                    isPending -> TripStage.WAITING_FOR_DRIVER
                    booking.status == "picked_up" -> TripStage.IN_TRANSIT
                    booking.status in listOf("done", "completed") -> TripStage.ARRIVED_DESTINATION
                    else -> TripStage.PICKUP_EN_ROUTE
                }

                val pLat = booking.pickupLat ?: _uiState.value.pickupLat
                val pLng = booking.pickupLng ?: _uiState.value.pickupLng
                val dLat = booking.dropoffLat ?: _uiState.value.destinationLat
                val dLng = booking.dropoffLng ?: _uiState.value.destinationLng

                _uiState.update { current ->
                    current.copy(
                        driverName = booking.driverName.ifBlank { current.driverName },
                        vehicleModel = booking.vehicleModel.ifBlank { current.vehicleModel },
                        vehiclePlate = booking.vehiclePlate.ifBlank { current.vehiclePlate },
                        bookingPin = booking.pickupPin.ifBlank { current.bookingPin },
                        pickupLocation = if (booking.pickupAddress.isNotBlank()) "Jemput: ${booking.pickupAddress}" else current.pickupLocation,
                        destinationLocation = if (booking.dropoffAddress.isNotBlank()) "${booking.dropoffAddress} (Tujuan)" else current.destinationLocation,
                        vehicleType = if (isMotor) VehicleType.MOTORCYCLE else VehicleType.CAR,
                        isCurrentUserDriver = isDriver,
                        tripStage = initialStage,
                        statusText = when (initialStage) {
                            TripStage.WAITING_FOR_DRIVER -> "Menunggu Konfirmasi Pengemudi..."
                            TripStage.IN_TRANSIT -> "Dalam Perjalanan ke Tujuan"
                            TripStage.ARRIVED_DESTINATION -> "Tiba di Titik Tujuan"
                            else -> if (isDriver) "Menuju Titik Penjemputan" else "Driver Sedang Menjemput"
                        },
                        pickupLat = pLat,
                        pickupLng = pLng,
                        destinationLat = dLat,
                        destinationLng = dLng
                    )
                }

                if (!isPending && initialStage == TripStage.PICKUP_EN_ROUTE) {
                    val dStartLat = pLat - 0.0035
                    val dStartLng = pLng - 0.0030
                    startLiveTrackingSimulation(
                        startLat = dStartLat,
                        startLng = dStartLng,
                        targetLat = pLat,
                        targetLng = pLng
                    )
                }
            }
        }
    }

    private fun createInitialState(id: String): LiveTrackingUiState {
        return LiveTrackingUiState(
            bookingId = id,
            driverName = "Pengemudi",
            vehicleModel = "",
            vehiclePlate = "",
            vehicleType = VehicleType.CAR,
            etaMinutes = 3,
            distanceMeters = 450,
            pickupLocation = "Titik Jemput",
            destinationLocation = "Titik Tujuan",
            bookingPin = "",
            statusText = "Driver Sedang Menjemput"
        )
    }
}

