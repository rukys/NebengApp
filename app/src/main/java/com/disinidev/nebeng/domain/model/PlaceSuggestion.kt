package com.disinidev.nebeng.domain.model

data class PlaceSuggestion(
    val id: String,
    val name: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double
)
