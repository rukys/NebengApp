package com.disinidev.nebeng.domain.model

import java.time.Instant

data class Payment(
    val id: String,
    val bookingId: String,
    val userId: String,
    val amount: Int,
    val method: PaymentMethod,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val midtransOrderId: String? = null,
    val midtransTransactionId: String? = null,
    val qrCodeUrl: String? = null,
    val expiredAt: Instant? = null,
    val paidAt: Instant? = null,
    val createdAt: Instant = Instant.now()
)

enum class PaymentMethod(val value: String, val displayName: String) {
    QRIS("qris", "QRIS"),
    BCA_TRANSFER("bca_transfer", "BCA Virtual Account"),
    MANDIRI_TRANSFER("mandiri_transfer", "Mandiri Virtual Account"),
    BRI_TRANSFER("bri_transfer", "BRI Virtual Account"),
    BNI_TRANSFER("bni_transfer", "BNI Virtual Account"),
    GOPAY("gopay", "GoPay"),
    OVO("ovo", "OVO"),
    SHOPEEPAY("shopeepay", "ShopeePay"),
    DANA("dana", "DANA"),
    TRANSFER("transfer", "Bank Transfer");

    companion object {
        fun fromString(value: String): PaymentMethod = entries.firstOrNull {
            it.value.equals(value, ignoreCase = true) || it.name.equals(value, ignoreCase = true)
        } ?: QRIS
    }
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    EXPIRED,
    REFUNDED;

    companion object {
        fun fromString(value: String): PaymentStatus = runCatching {
            valueOf(value.uppercase())
        }.getOrDefault(PENDING)
    }
}
