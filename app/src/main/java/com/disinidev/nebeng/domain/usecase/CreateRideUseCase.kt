package com.disinidev.nebeng.domain.usecase

import com.disinidev.nebeng.domain.repository.CreateRideRequest
import com.disinidev.nebeng.domain.repository.RideRepository
import com.disinidev.nebeng.domain.repository.UserRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class CreateRideUseCase @Inject constructor(
    private val rideRepository: RideRepository,
    private val userRepository: UserRepository
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

        val driverUuid = userRepository.getCurrentUserUuid()
        val isoDepartureTime = parseDepartureTimeToIso(departureTime)

        val request = CreateRideRequest(
            driverId = driverUuid,
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
            departureTime = isoDepartureTime,
            notes = notes
        )
        return rideRepository.createRide(request)
    }

    private fun parseDepartureTimeToIso(timeStr: String): String {
        return try {
            val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
            val timeRegex = Regex("(\\d{1,2}):(\\d{2})")
            val match = timeRegex.find(timeStr)
            if (match != null) {
                val (h, m) = match.destructured
                val localTime = LocalTime.of(h.toInt(), m.toInt())
                val zonedDateTime = ZonedDateTime.of(today, localTime, ZoneId.of("Asia/Jakarta"))
                zonedDateTime.toInstant().toString()
            } else {
                Instant.now().plusSeconds(1800).toString()
            }
        } catch (_: Exception) {
            Instant.now().plusSeconds(1800).toString()
        }
    }
}
