package com.disinidev.nebeng.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OsmNominatimPlace(
    @SerialName("place_id") val placeId: Long = 0,
    @SerialName("name") val name: String? = null,
    @SerialName("display_name") val displayName: String = "",
    @SerialName("lat") val lat: String = "0.0",
    @SerialName("lon") val lon: String = "0.0"
)
