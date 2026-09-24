package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.CreateRideRequest
import com.disinidev.nebeng.domain.repository.RideRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class CreateRideUseCase @Inject constructor(
    private val rideRepository: RideRepository,
    private val firebaseAuth: FirebaseAuth
) {
    suspend operator fun invoke(
        pickupAddress: String,
        pickupLat: Double,
        pickupLng: Double,
        dropoffAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        vehicleBrand: String,
        vehicleModel: String,
        vehiclePlate: String,
        vehicleType: String,
        availableSeats: Int,
        departureTime: String,
        notes: String?
    ): Result<String> {
        if (pickupAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("Titik jemput tidak boleh kosong"))
        }
        if (dropoffAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("Titik tujuan tidak boleh kosong"))
        }
        if (vehiclePlate.isBlank()) {
            return Result.failure(IllegalArgumentException("Plat nomor kendaraan harus diisi"))
        }
        if (availableSeats <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah kursi harus minimal 1"))
        }

        val driverId = firebaseAuth.currentUser?.uid ?: "00000000-0000-0000-0000-000000000002"
        val request = CreateRideRequest(
            driverId = driverId,
            vehicleBrand = vehicleBrand.ifBlank { "Toyota" },
            vehicleModel = vehicleModel.ifBlank { if (vehicleType == "motorcycle") "Yamaha NMAX" else "Avanza" },
            vehiclePlate = vehiclePlate.uppercase(),
            vehicleType = vehicleType,
            maxPassengers = availableSeats,
            availableSeats = availableSeats,
            pickupAddress = pickupAddress,
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            dropoffAddress = dropoffAddress,
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng,
            departureTime = departureTime,
            notes = notes
        )
        return rideRepository.createRide(request)
    }
}
