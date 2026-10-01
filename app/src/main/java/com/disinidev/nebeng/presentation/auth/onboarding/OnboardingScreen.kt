package com.disinidev.nebeng.presentation.auth.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.core.designsystem.NebengSpacing
import kotlinx.coroutines.launch

data class OnboardingStep(
    val tag: String,
    val title: String,
    val description: String,
    val ctaLabel: String
)

object OnboardingDefaults {
    val Steps = listOf(
        OnboardingStep(
            tag = "RUTE SEARAH",
            title = "Berangkat kerja nggak harus macet sendirian.",
            description = "Temukan rekan satu rute setiap pagi. Perjalanan lebih santai, tiba tepat waktu bersama komunitas.",
            ctaLabel = "Lanjutkan"
        ),
        OnboardingStep(
            tag = "BERBAGI KURSI",
            title = "Ada kursi kosong?\nBagi bareng sesama.",
            description = "Bawa kendaraan jadi lebih bermanfaat. Kurangi polusi dan hemat biaya perjalanan bersama.",
            ctaLabel = "Lanjutkan"
        ),
        OnboardingStep(
            tag = "AMAN & NYAMAN",
            title = "Tenang, semua teman tebengan terverifikasi.",
            description = "Identitas resmi tervalidasi dan kode jemput otomatis. Berangkat aman, pulang tenang.",
            ctaLabel = "Mulai Sekarang"
        )
    )
}

/**
 * Stateful entry point for Onboarding screen.
 */
@Composable
fun OnboardingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val steps = OnboardingDefaults.Steps
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    OnboardingContent(
        pagerState = pagerState,
        steps = steps,
        onSkip = {
            viewModel.completeOnboarding()
            onNavigateToRegister()
        },
        onNext = {
            if (pagerState.currentPage < steps.size - 1) {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            } else {
                viewModel.completeOnboarding()
                onNavigateToRegister()
            }
        },
        onLogin = {
            viewModel.completeOnboarding()
            onNavigateToLogin()
        },
        heroContent = { pageIndex ->
            when (pageIndex) {
                0 -> RouteConvergenceIllustration()
                1 -> SharedSeatsIllustration()
                2 -> TrustSecurityIllustration()
                else -> Unit
            }
        },
        modifier = modifier
    )
}

/**
 * Stateless, testable, previewable Onboarding layout adhering to compose-component-design.
 */
@Composable
fun OnboardingContent(
    pagerState: PagerState,
    steps: List<OnboardingStep>,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
    heroContent: @Composable (pageIndex: Int) -> Unit = { pageIndex ->
        when (pageIndex) {
            0 -> RouteConvergenceIllustration()
            1 -> SharedSeatsIllustration()
            2 -> TrustSecurityIllustration()
            else -> Unit
        }
    }
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = NebengColor.Primary0
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = NebengSpacing.Xxl)
        ) {
            // Header: Brand Wordmark + 48dp Skip Button
            OnboardingHeader(onSkipClick = onSkip)

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Progress Indicators
            OnboardingPageIndicator(
                pageCount = steps.size,
                currentPage = pagerState.currentPage
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Horizontal Pager (Hero Art + Value Proposition)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                val step = steps[pageIndex]
                OnboardingPageItem(
                    step = step,
                    heroContent = { heroContent(pageIndex) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom CTA actions (Thumb-Zone)
            val currentStep = steps[pagerState.currentPage]
            OnboardingBottomBar(
                ctaLabel = currentStep.ctaLabel,
                onPrimaryClick = onNext,
                onLoginClick = onLogin
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Minimalist top header with brand identity and accessible touch targets.
 */
@Composable
fun OnboardingHeader(
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nebeng",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = NebengColor.Primary900,
            letterSpacing = (-0.5).sp
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSkipClick
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Lewati",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NebengColor.Gray600
            )
        }
    }
}

/**
 * Smooth animated pill progress indicator.
 */
@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 28.dp else 8.dp,
                animationSpec = tween(durationMillis = 250),
                label = "pill_width_$index"
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(width)
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(
                        if (isSelected) NebengColor.Primary900 else NebengColor.Gray200
                    )
            )
        }
    }
}

/**
 * Single page item owning invariant layout; delegates visual illustration to slot.
 */
@Composable
fun OnboardingPageItem(
    step: OnboardingStep,
    heroContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Hero Visual Area (Slot API)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            contentAlignment = Alignment.Center
        ) {
            heroContent()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Editorial Typography
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Subtle Eyebrow Tag
            Text(
                text = step.tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = NebengColor.Gray400
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = step.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NebengColor.Gray800,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Conversational Human Body Copy
            Text(
                text = step.description,
                fontSize = 14.sp,
                color = NebengColor.Gray600,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Bottom action controls with thumb-zone ergonomics.
 */
@Composable
fun OnboardingBottomBar(
    ctaLabel: String,
    onPrimaryClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(NebengRadius.Lg),
            colors = ButtonDefaults.buttonColors(
                containerColor = NebengColor.Primary900,
                contentColor = NebengColor.Primary0
            ),
            elevation = null
        ) {
            AnimatedContent(
                targetState = ctaLabel,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "cta_label"
            ) { label ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sudah punya akun? ",
                    fontSize = 13.sp,
                    color = NebengColor.Gray600
                )
                Text(
                    text = "Masuk",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onLoginClick
                        )
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

// ==============================================================================
// EDITORIAL MINIMALIST HERO ILLUSTRATIONS (Clean, Human, Authentic)
// ==============================================================================

/**
 * Slide 1: Route & Commute Synchrony.
 * Organic concentric orbits with converging traveler nodes.
 */
@Composable
fun RouteConvergenceIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ambient halo
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
        )

        // Mid orbital track
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(1.dp, NebengColor.Gray200, CircleShape)
        )

        // Center hub
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary0)
                .border(1.5.dp, NebengColor.Primary900, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                tint = NebengColor.Primary900,
                modifier = Modifier.size(34.dp)
            )
        }

        // Floating Commuter Node A (Driver)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = 30.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(22.dp)
            )
        }

        // Floating Commuter Node B (Passenger)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-24).dp, y = (-30).dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary0)
                .border(1.dp, NebengColor.Gray200, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = NebengColor.Gray800,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Slide 2: Shared Empty Seats & Community Solidarity.
 * Minimalist geometric car cabin silhouette with active welcoming seats.
 */
@Composable
fun SharedSeatsIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient background
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
        )

        // Car Cabin Pill
        Box(
            modifier = Modifier
                .width(170.dp)
                .height(190.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(NebengColor.Primary0)
                .border(1.5.dp, NebengColor.Gray200, RoundedCornerShape(40.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Front Row (Driver + Passenger)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MinimalistSeat(isDriver = true)
                    MinimalistSeat(isDriver = false, isAvailable = true)
                }

                // Center divider / aisle indicator
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(NebengColor.Gray200)
                )

                // Back Row (Left + Right Passengers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MinimalistSeat(isDriver = false, isAvailable = true)
                    MinimalistSeat(isDriver = false, isAvailable = true)
                }
            }
        }

        // Floating solidarity pill badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 10.dp)
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(NebengColor.Primary900)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "3 Kursi Terbuka",
                color = NebengColor.Primary0,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MinimalistSeat(
    isDriver: Boolean,
    modifier: Modifier = Modifier,
    isAvailable: Boolean = false
) {
    Box(
        modifier = modifier
            .size(width = 54.dp, height = 58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isDriver -> NebengColor.Primary900
                    isAvailable -> NebengColor.Primary50
                    else -> NebengColor.Gray200
                }
            )
            .border(
                width = 1.dp,
                color = if (isDriver) NebengColor.Primary900 else NebengColor.Gray200,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isDriver) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(18.dp)
            )
        } else if (isAvailable) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
            )
        }
    }
}

/**
 * Slide 3: Trust & Safety.
 * Serene verification emblem with validated credentials.
 */
@Composable
fun TrustSecurityIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ambient ring
        Box(
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary50)
        )

        // Mid ring
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(1.dp, NebengColor.Gray200, CircleShape)
        )

        // Center Security Shield
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(NebengColor.Primary900),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NebengColor.Primary0,
                modifier = Modifier.size(42.dp)
            )
        }

        // Satellite Badge: KYC e-KTP
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = 28.dp)
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(NebengColor.Primary0)
                .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Full))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "e-KTP Valid",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Gray800
                )
            }
        }

        // Satellite Badge: PIN Jemput
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-26).dp)
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(NebengColor.Primary0)
                .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Full))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PIN Jemput",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Gray800
                )
            }
        }
    }
}

// ==============================================================================
// PREVIEWS
// ==============================================================================

@Preview(showBackground = true)
@Composable
private fun OnboardingContentPreview() {
    OnboardingContent(
        pagerState = rememberPagerState(pageCount = { OnboardingDefaults.Steps.size }),
        steps = OnboardingDefaults.Steps,
        onSkip = {},
        onNext = {},
        onLogin = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun RouteIllustrationPreview() {
    RouteConvergenceIllustration(modifier = Modifier.padding(24.dp))
}

@Preview(showBackground = true)
@Composable
private fun SharedSeatsIllustrationPreview() {
    SharedSeatsIllustration(modifier = Modifier.padding(24.dp))
}

@Preview(showBackground = true)
@Composable
private fun TrustSecurityIllustrationPreview() {
    TrustSecurityIllustration(modifier = Modifier.padding(24.dp))
}
