package com.disinidev.nebeng.presentation.auth.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val icon: ImageVector,
    val badgeIcon: ImageVector,
    val badgeIconTint: Color,
    val badgeText: String,
    val title: String,
    val description: String,
    val buttonText: String
)

private val OnboardingPages = listOf(
    OnboardingPageData(
        icon = Icons.Default.DirectionsCar,
        badgeIcon = Icons.Default.Bolt,
        badgeIconTint = Color(0xFFFFD700),
        badgeText = "Hemat s/d 70% Ongkos",
        title = "Nebeng Hemat Harian",
        description = "Pilihan tumpangan mobil atau motor searah ke tempat kerja. Lebih nyaman, cepat, dan hemat ongkos harian.",
        buttonText = "Lanjut nebeng"
    ),
    OnboardingPageData(
        icon = Icons.Default.LocalGasStation,
        badgeIcon = Icons.Default.Payments,
        badgeIconTint = Color(0xFF4CAF50),
        badgeText = "+Rp 480.000 / minggu",
        title = "Beri Tebengan, Dapat Bensin",
        description = "Buka kursi kosong saat berangkat kerja dan dapatkan uang patungan bensin otomatis dari rekan komuter.",
        buttonText = "Jadi pengemudi"
    ),
    OnboardingPageData(
        icon = Icons.Default.Security,
        badgeIcon = Icons.Default.Check,
        badgeIconTint = Color(0xFF4CAF50),
        badgeText = "100% Terverifikasi & SOS 24/7",
        title = "Perjalanan Aman Terverifikasi",
        description = "Seluruh pengguna telah terverifikasi KTP & kantor. Dilengkapi fitur PIN penjemputan dan Tombol Darurat SOS.",
        buttonText = "Mulai sekarang"
    )
)

@Composable
fun OnboardingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NebengColor.Primary0)
    ) {
        // --- TOP HALF (WHITE BACKGROUND) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
                .background(NebengColor.Primary0)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 3 Dash Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(OnboardingPages.size) { index ->
                    val isActive = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(NebengRadius.Full))
                            .background(if (isActive) NebengColor.Primary900 else NebengColor.Gray200)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Illustration Circle + Badge
            val currentPage = OnboardingPages[pagerState.currentPage]

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(NebengColor.Primary50),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(NebengColor.Primary900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentPage.icon,
                        contentDescription = null,
                        tint = NebengColor.Primary0,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Black Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(NebengColor.Primary900)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = currentPage.badgeIcon,
                        contentDescription = null,
                        tint = currentPage.badgeIconTint,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentPage.badgeText,
                        color = NebengColor.Primary0,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // --- BOTTOM HALF (BLACK BACKGROUND) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = NebengRadius.Xxl, topEnd = NebengRadius.Xxl))
                .background(NebengColor.Primary900)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val page = OnboardingPages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = page.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary0,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.description,
                        fontSize = 13.sp,
                        color = Color(0xFFA0A0A0),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentPage = OnboardingPages[pagerState.currentPage]

                // White Pill CTA Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < OnboardingPages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onNavigateToRegister()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(NebengRadius.Full),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NebengColor.Primary0,
                        contentColor = NebengColor.Primary900
                    ),
                    elevation = null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentPage.buttonText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowOutward,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer "Sudah punya akun? Masuk"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sudah punya akun? ",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                    Text(
                        text = "Masuk",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary0,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNavigateToLogin
                        )
                    )
                }
            }
        }
    }
}
