package com.disinidev.nebeng.data.model

import com.disinidev.nebeng.domain.model.VehicleType
import com.disinidev.nebeng.presentation.search.model.DriverGender
import com.disinidev.nebeng.presentation.search.model.RideItemUi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class RideSearchResultDto(
    @SerialName("id") val id: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("driver_name") val driverName: String,
    @SerialName("driver_gender") val driverGender: String = "male",
    @SerialName("driver_avatar") val driverAvatar: String? = null,
    @SerialName("driver_rating") val driverRating: Double = 5.0,
    @SerialName("driver_total_trips") val driverTotalTrips: Int = 0,
    @SerialName("vehicle_brand") val vehicleBrand: String,
    @SerialName("vehicle_model") val vehicleModel: String,
    @SerialName("vehicle_plate") val vehiclePlate: String,
    @SerialName("vehicle_type") val vehicleType: String,
    @SerialName("max_passengers") val maxPassengers: Int,
    @SerialName("available_seats") val availableSeats: Int,
    @SerialName("pickup_address") val pickupAddress: String,
    @SerialName("pickup_lat") val pickupLat: Double,
    @SerialName("pickup_lng") val pickupLng: Double,
    @SerialName("dropoff_address") val dropoffAddress: String,
    @SerialName("dropoff_lat") val dropoffLat: Double,
    @SerialName("dropoff_lng") val dropoffLng: Double,
    @SerialName("departure_time") val departureTime: String,
    @SerialName("notes") val notes: String? = null,
    @SerialName("distance_meters") val distanceMeters: Double? = null
) {
    fun toUiModel(): RideItemUi {
        val departureInstant = runCatching { Instant.parse(departureTime) }.getOrNull() ?: Instant.now()
        val departureZoned = departureInstant.atZone(ZoneId.of("Asia/Jakarta"))
        val arrivalZoned = departureZoned.plusMinutes(40)

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val depFormatted = departureZoned.format(timeFormatter)
        val arrFormatted = arrivalZoned.format(timeFormatter)

        val vType = if (vehicleType.equals("motorcycle", ignoreCase = true)) {
            VehicleType.MOTORCYCLE
        } else {
            VehicleType.CAR
        }

        val gender = if (driverGender.equals("female", ignoreCase = true)) {
            DriverGender.FEMALE
        } else {
            DriverGender.MALE
        }

        val facilitiesList = mutableListOf<String>()
        if (notes != null) {
            facilitiesList.addAll(notes.split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
        if (facilitiesList.isEmpty()) {
            if (vType == VehicleType.CAR) {
                facilitiesList.addAll(listOf("Non-smoking", "Full AC", "Kantor SCBD"))
            } else {
                facilitiesList.addAll(listOf("Helm Tersedia", "Jas Hujan"))
            }
        }

        val seatsText = if (vType == VehicleType.MOTORCYCLE) {
            "1 Kursi"
        } else {
            "Sisa $availableSeats Kursi"
        }

        return RideItemUi(
            id = id,
            driverName = driverName,
            driverGender = gender,
            vehicleModel = "$vehicleBrand $vehicleModel ($vehiclePlate)",
            vehicleType = vType,
            departureTimeFormatted = depFormatted,
            arrivalTimeFormatted = arrFormatted,
            availableSeats = availableSeats,
            availableSeatsText = seatsText,
            facilities = facilitiesList,
            driverRating = driverRating,
            totalTrips = driverTotalTrips,
            isOfficeVerified = true
        )
    }
}
