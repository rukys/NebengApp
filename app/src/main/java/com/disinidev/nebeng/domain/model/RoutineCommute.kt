package com.disinidev.nebeng.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RoutineCommute(
    val id: String,
    val userId: String,
    val originName: String,
    val destinationName: String,
    val originLat: Double = 0.0,
    val originLng: Double = 0.0,
    val destinationLat: Double = 0.0,
    val destinationLng: Double = 0.0,
    val departureTime: String, // HH:mm
    val activeDays: List<Int>, // 1 = Senin, ..., 7 = Minggu
    val vehicleType: String = "car", // "car" or "motorcycle"
    val isEnabled: Boolean = true,
    val autoBook: Boolean = false,
    val createdAt: String? = null
) {
    fun getFormattedDays(): String {
        if (activeDays.isEmpty()) return "Tidak ada hari dipilih"
        if (activeDays.sorted() == listOf(1, 2, 3, 4, 5)) return "Senin - Jumat (Hari Kerja)"
        if (activeDays.sorted() == listOf(6, 7)) return "Sabtu - Minggu (Akhir Pekan)"
        if (activeDays.size == 7) return "Setiap Hari"

        val dayNames = mapOf(
            1 to "Sen",
            2 to "Sel",
            3 to "Rab",
            4 to "Kam",
            5 to "Jum",
            6 to "Sab",
            7 to "Min"
        )
        return activeDays.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }
}
