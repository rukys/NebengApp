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

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookingRepository: BookingRepository,
    private val observeDriverLocationUseCase: ObserveDriverLocationUseCase,
    private val updateDriverLocationUseCase: UpdateDriverLocationUseCase,
    private val locationClient: LocationClient
) : ViewModel() {

    private val bookingId: String = savedStateHandle.get<String>("bookingId") ?: "booking_ride_1"

    private val _uiState = MutableStateFlow(createInitialState(bookingId))
    val uiState: StateFlow<LiveTrackingUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private var observeJob: Job? = null

    init {
        loadBooking()
        initializeTracking()
        observeDriverLocationStream()
    }

    fun showEmergencyDialog(show: Boolean) {
        _uiState.update { it.copy(isEmergencyDialogOpen = show) }
    }

    /**
     * Initializes pickup point to real device GPS location if available,
     * and sets driver starting position ~450m away approaching the pickup point.
     */
    private fun initializeTracking() {
        viewModelScope.launch {
            val loc = if (locationClient.hasLocationPermission()) {
                locationClient.getCurrentLocation()
            } else null

            val pLat = loc?.latitude ?: _uiState.value.pickupLat
            val pLng = loc?.longitude ?: _uiState.value.pickupLng

            // Driver starts ~400-500m away from pickup point approaching along the road
            val dStartLat = pLat - 0.0035
            val dStartLng = pLng - 0.0030

            // Destination is ~2km away
            val dstLat = pLat + 0.0120
            val dstLng = pLng + 0.0150

            _uiState.update { current ->
                current.copy(
                    pickupLat = pLat,
                    pickupLng = pLng,
                    driverCurrentLat = dStartLat,
                    driverCurrentLng = dStartLng,
                    destinationLat = dstLat,
                    destinationLng = dstLng,
                    distanceMeters = 450,
                    etaMinutes = 3,
                    statusText = "Driver Sedang Menjemput",
                    progress = 0.0f,
                    isArrived = false
                )
            }

            // Sync driver initial location
            updateDriverLocationUseCase(bookingId, dStartLat, dStartLng)

            // Start smooth driver simulation approaching user's pickup point
            startLiveTrackingSimulation(
                startLat = dStartLat,
                startLng = dStartLng,
                targetLat = pLat,
                targetLng = pLng
            )
        }
    }

    /**
     * Listens to live location updates for this booking from Supabase trip_locations table.
     */
    private fun observeDriverLocationStream() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeDriverLocationUseCase(bookingId).collect { tripLoc ->
                if (tripLoc != null) {
                    val pLat = _uiState.value.pickupLat
                    val pLng = _uiState.value.pickupLng
                    val dist = calculateDistanceMeters(tripLoc.lat, tripLoc.lng, pLat, pLng)
                    // Discard stale remote locations (> 15km away)
                    if (dist <= 15000) {
                        simulationJob?.cancel()
                        onRemoteDriverLocationReceived(tripLoc)
                    }
                }
            }
        }
    }

    private fun onRemoteDriverLocationReceived(tripLoc: TripLocation) {
        val pickupLat = _uiState.value.pickupLat
        val pickupLng = _uiState.value.pickupLng
        val distMeters = calculateDistanceMeters(tripLoc.lat, tripLoc.lng, pickupLat, pickupLng)
        val etaMin = maxOf(0, (distMeters / 150))
        val isArrived = distMeters <= 25

        _uiState.update { current ->
            current.copy(
                driverCurrentLat = tripLoc.lat,
                driverCurrentLng = tripLoc.lng,
                distanceMeters = distMeters,
                etaMinutes = etaMin,
                statusText = if (isArrived) "Driver Telah Tiba di Titik Jemput!" else "Driver Sedang Menjemput",
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
                TrackingStep(progress = 0.0f, distance = 450, eta = 3, status = "Driver Sedang Menjemput", bearing = 45f),
                TrackingStep(progress = 0.25f, distance = 340, eta = 3, status = "Driver Sedang Menjemput", bearing = 48f),
                TrackingStep(progress = 0.55f, distance = 210, eta = 2, status = "Driver Mendekati Titik Jemput", bearing = 40f),
                TrackingStep(progress = 0.82f, distance = 80, eta = 1, status = "Driver Hampir Sampai (80m)", bearing = 45f),
                TrackingStep(progress = 1.0f, distance = 0, eta = 0, status = "Driver Telah Tiba di Titik Jemput!", bearing = 45f, isArrived = true)
            )

            for (step in steps) {
                delay(3500)
                val curLat = startLat + (targetLat - startLat) * step.progress
                val curLng = startLng + (targetLng - startLng) * step.progress

                // Sync live position to Supabase trip_locations
                updateDriverLocationUseCase(bookingId, curLat, curLng)

                _uiState.update { current ->
                    current.copy(
                        progress = step.progress,
                        distanceMeters = step.distance,
                        etaMinutes = step.eta,
                        statusText = step.status,
                        driverBearing = step.bearing,
                        driverCurrentLat = curLat,
                        driverCurrentLng = curLng,
                        isArrived = step.isArrived
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
        val isArrived: Boolean = false
    )

    private fun loadBooking() {
        viewModelScope.launch {
            val result = bookingRepository.getBookingById(bookingId)
            result.getOrNull()?.let { booking ->
                val isMotor = booking.vehicleModel.contains("NMAX", ignoreCase = true) || booking.seatPosition == "pillion"
                _uiState.update { current ->
                    current.copy(
                        driverName = booking.driverName,
                        vehicleModel = booking.vehicleModel,
                        vehiclePlate = booking.vehiclePlate,
                        bookingPin = booking.pickupPin,
                        vehicleType = if (isMotor) VehicleType.MOTORCYCLE else VehicleType.CAR
                    )
                }
            }
        }
    }

    private fun createInitialState(id: String): LiveTrackingUiState {
        return if (id.contains("ride_2")) {
            LiveTrackingUiState(
                bookingId = id,
                driverName = "Reza H.",
                vehicleModel = "Yamaha NMAX Hitam",
                vehiclePlate = "B 5678 XYZ",
                vehicleType = VehicleType.MOTORCYCLE,
                etaMinutes = 2,
                distanceMeters = 300,
                pickupLocation = "Jemput: Halte Gelora",
                destinationLocation = "SCBD Lot 8 (Tujuan)",
                bookingPin = "215 889",
                statusText = "Driver Sedang Menjemput"
            )
        } else {
            LiveTrackingUiState(
                bookingId = id,
                driverName = "Andi P.",
                vehicleModel = "Avanza Silver",
                vehiclePlate = "B 1234 ABC",
                vehicleType = VehicleType.CAR,
                etaMinutes = 3,
                distanceMeters = 450,
                pickupLocation = "Jemput: Pintu Barat Lawson",
                destinationLocation = "SCBD Lot 8 (Tujuan)",
                bookingPin = "489 201",
                statusText = "Driver Sedang Menjemput"
            )
        }
    }
}

