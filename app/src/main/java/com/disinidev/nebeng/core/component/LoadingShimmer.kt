package com.disinidev.nebeng.core.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius

val ShimmerColors = listOf(
    NebengColor.Primary50,
    NebengColor.Gray100,
    NebengColor.Primary50
)

@Composable
fun rememberShimmerBrush(
    targetValue: Float = 1200f,
    durationMillis: Int = 1200
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = ShimmerColors,
        start = Offset(x = translateAnim - 350f, y = translateAnim - 350f),
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

fun Modifier.shimmer(
    shape: Shape = RoundedCornerShape(NebengRadius.Md)
): Modifier = composed {
    val brush = rememberShimmerBrush()
    this.clip(shape).background(brush)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    cornerRadius: Dp = NebengRadius.Sm
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun LoadingShimmer(
    height: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = NebengRadius.Md
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * RideCardSkeleton - Used in Home & Search Results list
 */
@Composable
fun RideCardSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp)
    ) {
        // Driver header: Avatar + Name + Rating placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Route: Origin dot -> vertical line -> Dest dot
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom row: Time chip + Free badge / button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(NebengRadius.Md))
                    .background(brush)
            )
        }
    }
}

/**
 * ConversationItemSkeleton - Used in Pesan & Obrolan list
 */
@Composable
fun ConversationItemSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Md))
            .background(NebengColor.Primary0)
            .border(0.5.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Md))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(brush)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(brush)
        )
    }
}

/**
 * ActivityTripSkeleton - Used in Aktivitas screen loading state
 */
@Composable
fun ActivityTripSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp)
    ) {
        // Status badge row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(55.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Counterpart row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Route bars
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(12.dp)
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(12.dp)
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action button placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(brush)
        )
    }
}

/**
 * RoutineCardSkeleton - Used in RoutineCommuteScreen loading state
 */
@Composable
fun RoutineCardSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(brush)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .clip(RoundedCornerShape(NebengRadius.Sm))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(NebengRadius.Md))
                .background(brush)
        )
    }
}

/**
 * VehicleCardSkeleton - Used in VehicleManagementScreen loading state
 */
@Composable
fun VehicleCardSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(NebengRadius.Sm))
                    .background(brush)
            )
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(brush)
        )
    }
}

/**
 * NotificationCardSkeleton - Used in NotificationScreen loading state
 */
@Composable
fun NotificationCardSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NebengRadius.Lg))
            .background(NebengColor.Primary0)
            .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Lg))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(NebengRadius.Sm))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(NebengRadius.Sm))
                            .background(brush)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(NebengRadius.Sm))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(NebengRadius.Full))
                        .background(brush)
                )
            }
        }
    }
}

