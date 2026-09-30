package com.disinidev.nebeng.domain.model

import java.time.Instant

data class Voucher(
    val id: String,
    val code: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val maxDiscount: Int? = null,
    val minOrder: Int = 0,
    val usageLimit: Int? = null,
    val usedCount: Int = 0,
    val validFrom: Instant,
    val validUntil: Instant,
    val isActive: Boolean = true
) {
    fun calculateDiscount(basePrice: Int): Int {
        if (!isActive || Instant.now().isAfter(validUntil) || Instant.now().isBefore(validFrom)) return 0
        if (basePrice < minOrder) return 0
        val discount = when (discountType) {
            DiscountType.FIXED -> discountValue
            DiscountType.PERCENTAGE -> (basePrice * discountValue) / 100
        }
        return if (maxDiscount != null) minOf(discount, maxDiscount) else discount
    }
}

enum class DiscountType {
    FIXED,
    PERCENTAGE;

    companion object {
        fun fromString(value: String): DiscountType = when (value.lowercase()) {
            "percentage" -> PERCENTAGE
            else -> FIXED
        }
    }
}
