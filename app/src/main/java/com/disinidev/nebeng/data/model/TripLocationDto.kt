package com.disinidev.nebeng.data.model

import com.disinidev.nebeng.domain.model.TripLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TripLocationDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("booking_id")
    val bookingId: String,
    @SerialName("lat")
    val lat: Double,
    @SerialName("lng")
    val lng: Double,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun toDomain(): TripLocation {
        return TripLocation(
            id = id,
            bookingId = bookingId,
            lat = lat,
            lng = lng,
            updatedAt = runCatching {
                if (updatedAt != null) Instant.parse(updatedAt) else Instant.now()
            }.getOrDefault(Instant.now())
        )
    }
}
