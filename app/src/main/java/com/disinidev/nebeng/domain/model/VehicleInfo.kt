package com.disinidev.nebeng.domain.model

data class VehicleInfo(
    val id: String? = null,
    val brand: String,
    val model: String,
    val plate: String,
    val type: VehicleType,
    val color: String? = null,
    val year: Int? = null,
    val isVerified: Boolean = false
)

enum class VehicleType {
    CAR,
    MOTORCYCLE;

    companion object {
        fun fromString(value: String): VehicleType = when (value.lowercase()) {
            "motorcycle" -> MOTORCYCLE
            else -> CAR
        }
    }
}
