package com.disinidev.nebeng.domain.model

import java.time.Instant

data class TripLocation(
    val id: String? = null,
    val bookingId: String,
    val lat: Double,
    val lng: Double,
    val updatedAt: Instant = Instant.now()
)
