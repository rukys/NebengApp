package com.disinidev.nebeng.core.component

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing

enum class FaqCategory(val label: String) {
    ALL("Semua"),
    RIDES_COST("Tebengan & Biaya"),
    BOOKING("Pemesanan"),
    DRIVER("Pengemudi"),
    SAFETY("Akun & Keamanan")
}

data class FaqData(
    val id: Int,
    val category: FaqCategory,
    val question: String,
    val answer: String
)

private val FAQ_DATABASE = listOf(
    FaqData(
        id = 1,
        category = FaqCategory.RIDES_COST,
        question = "Bagaimana sistem pembagian biaya (sharing cost) di Nebeng?",
        answer = "Nebeng adalah platform carpooling komuter, bukan taksi online komersial. Biaya yang tertera merupakan estimasi patungan bensin dan tol searah yang dihitung otomatis oleh sistem agar adil, transparan, dan hemat bagi semua pihak."
    ),
    FaqData(
        id = 2,
        category = FaqCategory.RIDES_COST,
        question = "Apakah pengemudi boleh meminta biaya tambahan di luar aplikasi?",
        answer = "Tidak boleh. Pengemudi dilarang meminta tarif tambahan di luar kesepakatan aplikasi. Jika terjadi pemaksaan atau tarif liar, tolak dan segera laporkan nomor plat pengemudi melalui menu Kontak Darurat & SOS."
    ),
    FaqData(
        id = 3,
        category = FaqCategory.RIDES_COST,
        question = "Bagaimana cara memberikan apresiasi tip ke pengemudi?",
        answer = "Setelah perjalanan selesai (Trip Selesai), Anda dapat memindai kode QRIS pengemudi langsung secara mandiri. Tip 100% diterima langsung oleh pengemudi tanpa potongan komisi platform."
    ),
    FaqData(
        id = 4,
        category = FaqCategory.BOOKING,
        question = "Bagaimana cara memesan tebengan mobil atau motor?",
        answer = "Pilih tab Mobil atau Motor di Beranda, tentukan lokasi penjemputan dan tujuan, pilih rute pengemudi yang searah dengan jam keberangkatan Anda, lalu lakukan konfirmasi pemesanan."
    ),
    FaqData(
        id = 5,
        category = FaqCategory.BOOKING,
        question = "Bagaimana jika pengemudi terlambat atau membatalkan perjalanan?",
        answer = "Sistem akan mengirimkan notifikasi instan dan Anda dapat memantau pergerakan pengemudi secara live. Jika pengemudi membatalkan, Anda dapat langsung mencari rute alternatif lain di sekitar Anda."
    ),
    FaqData(
        id = 6,
        category = FaqCategory.BOOKING,
        question = "Di mana titik penjemputan yang aman dan disarankan?",
        answer = "Pilihlah titik publik yang mudah diakses dan legal untuk berhenti sejenak, seperti lobi gedung perkantoran, halte busway/angkot, stasiun MRT/KRL, atau minimarket terdekat."
    ),
    FaqData(
        id = 7,
        category = FaqCategory.BOOKING,
        question = "Apa itu fitur Tebengan Rutin dan bagaimana cara kerjanya?",
        answer = "Tebengan Rutin memudahkan Anda menjadwalkan rute harian rumah-kantor (misal Senin–Jumat jam 07:00). Sistem otomatis mencocokkan tebengan setiap pagi tanpa perlu memesan manual setiap hari."
    ),
    FaqData(
        id = 8,
        category = FaqCategory.DRIVER,
        question = "Bagaimana cara menjadi pengemudi (Beri Tebeng)?",
        answer = "Ubah peran ke 'Pengemudi' di menu Profil, lakukan verifikasi e-KTP resmi dan daftarkan kendaraan Anda beserta nomor plat yang valid. Setelah diverifikasi, Anda bisa membagikan kursi kosong perjalanan harian Anda."
    ),
    FaqData(
        id = 9,
        category = FaqCategory.DRIVER,
        question = "Berapa kapasitas maksimal penumpang yang boleh dibawa?",
        answer = "Untuk moda motor maksimal 1 penumpang, dan untuk mobil disesuaikan dengan kapasitas kursi kosong yang didaftarkan (1–4 penumpang) demi kenyamanan dan keselamatan perjalanan bersama."
    ),
    FaqData(
        id = 10,
        category = FaqCategory.DRIVER,
        question = "Apakah pengemudi dikenakan komisi potongan dari aplikasi?",
        answer = "Nebeng tidak memotong komisi komersial dari pengemudi. Semua kontribusi patungan bensin dan apresiasi tip QRIS sepenuhnya menjadi hak pengemudi."
    ),
    FaqData(
        id = 11,
        category = FaqCategory.SAFETY,
        question = "Mengapa verifikasi e-KTP dan dokumen wajib dilakukan?",
        answer = "Verifikasi identitas memastikan seluruh pengguna dalam komunitas Nebeng adalah individu yang terverifikasi dan akuntabel, menciptakan lingkungan perjalanan yang aman dan saling percaya."
    ),
    FaqData(
        id = 12,
        category = FaqCategory.SAFETY,
        question = "Apa yang harus dilakukan jika terjadi situasi darurat di perjalanan?",
        answer = "Tekan tombol SOS di menu Profil atau di layar Live Tracking perjalanan. Sistem akan menyediakan panggilan cepat ke kontak darurat keluarga Anda serta hotline resmi Kepolisian (110 / 112)."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFaqBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FaqCategory.ALL) }
    var expandedFaqId by remember { mutableIntStateOf(1) } // First FAQ expanded by default

    val filteredFaqs = remember(searchQuery, selectedCategory) {
        FAQ_DATABASE.filter { item ->
            val matchCategory = selectedCategory == FaqCategory.ALL || item.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                item.question.contains(searchQuery, ignoreCase = true) ||
                item.answer.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = NebengColor.Primary0,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = NebengColor.Primary0,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pusat Bantuan & FAQ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                    Text(
                        text = "Panduan & solusi kendala seputar Nebeng",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(NebengColor.Primary50)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NebengColor.Gray600,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = NebengColor.Primary900
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari pertanyaan atau kendala...",
                                fontSize = 13.sp,
                                color = NebengColor.Gray400
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus",
                        tint = NebengColor.Gray600,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaqCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(NebengRadius.Full))
                            .background(if (isSelected) NebengColor.Primary900 else NebengColor.Primary50)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) NebengColor.Primary900 else NebengColor.Gray200,
                                shape = RoundedCornerShape(NebengRadius.Full)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = category.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NebengColor.Primary0 else NebengColor.Gray800
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // FAQ Items List
            if (filteredFaqs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pertanyaan tidak ditemukan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900
                    )
                    Text(
                        text = "Coba gunakan kata kunci lain atau hubungi tim bantuan kami.",
                        fontSize = 12.sp,
                        color = NebengColor.Gray600,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                filteredFaqs.forEach { faq ->
                    val isExpanded = expandedFaqId == faq.id
                    ExpandableFaqCard(
                        faq = faq,
                        isExpanded = isExpanded,
                        onToggle = {
                            expandedFaqId = if (isExpanded) 0 else faq.id
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Help Desk Contact Card (Hubungi Kami)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NebengRadius.Lg))
                    .background(NebengColor.Primary50)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Masih butuh bantuan lain?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900
                )
                Text(
                    text = "Tim Customer Care Nebeng siap membantu Anda setiap hari (06:00 - 22:00 WIB).",
                    fontSize = 12.sp,
                    color = NebengColor.Gray600,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // WhatsApp Support Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(NebengColor.Primary0)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                            .clickable {
                                runCatching {
                                    val url = "https://wa.me/6281234567890?text=Halo%20Tim%20Support%20Nebeng,%20saya%20butuh%20bantuan%20terkait%20aplikasi."
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Chat,
                            contentDescription = null,
                            tint = NebengColor.Success700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NebengColor.Primary900
                        )
                    }

                    // Email Support Button
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(NebengRadius.Md))
                            .background(NebengColor.Primary0)
                            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
                            .clickable {
                                runCatching {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:support@nebeng.id?subject=Pertanyaan%20Aplikasi%20Nebeng")
                                    }
                                    context.startActivity(emailIntent)
                                }
                            }
                            .padding(vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = NebengColor.Info600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Email Kami",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NebengColor.Primary900
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            NebengButton(
                text = "Tutup",
                onClick = onDismiss,
                style = NebengButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpandableFaqCard(
    faq: FaqData,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "faqChevronRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(if (isExpanded) NebengColor.Primary50 else NebengColor.Primary0)
            .border(
                width = 1.dp,
                color = if (isExpanded) NebengColor.Primary900.copy(alpha = 0.2f) else NebengColor.Gray200,
                shape = RoundedCornerShape(NebengRadius.Md)
            )
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NebengColor.Gray200)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = faq.category.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NebengColor.Gray800
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = faq.question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Tutup" else "Buka",
                tint = NebengColor.Gray600,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotationAngle)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(NebengColor.Gray200)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = faq.answer,
                    fontSize = 13.sp,
                    color = NebengColor.Gray800,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
