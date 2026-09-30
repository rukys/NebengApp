package com.disinidev.nebeng.domain.model

enum class CancellationReason(val label: String) {
    DRIVER_NOT_MOVING("Pengemudi tidak bergerak / tidak ada kabar"),
    SCHEDULE_CHANGED("Rencana atau jadwal perjalanan saya berubah"),
    WRONG_LOCATION("Salah menentukan titik jemput / tujuan"),
    FOUND_ANOTHER_RIDE("Sudah mendapat tebengan / transportasi lain"),
    DRIVER_ASKED_CANCEL("Pengemudi meminta perjalanan dibatalkan"),
    OTHER("Alasan lainnya")
}
