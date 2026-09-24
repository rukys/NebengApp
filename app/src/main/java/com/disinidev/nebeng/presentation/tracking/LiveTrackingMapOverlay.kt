package com.disinidev.nebeng.presentation.tracking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.disinidev.nebeng.core.designsystem.NebengColor
import com.disinidev.nebeng.core.designsystem.NebengRadius
import com.disinidev.nebeng.domain.model.VehicleType
import kotlin.math.roundToInt

@Composable
fun LiveTrackingMapOverlay(
    state: LiveTrackingUiState,
    onRecenterClick: () -> Unit,
    onGpsBadgeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Anchor points on screen (relative to viewport)
        // Destination: SCBD Lot 8 (Top)
        val destX = widthPx * 0.50f
        val destY = heightPx * 0.32f

        // Pickup point: Pintu Barat Lawson (Bottom)
        val pickupX = widthPx * 0.50f
        val pickupY = heightPx * 0.54f

        // Driver Start point (Middle)
        val startDriverX = widthPx * 0.50f
        val startDriverY = heightPx * 0.43f

        // Smoothly animated progress for 60fps butter-smooth vehicle movement
        val smoothProgress by animateFloatAsState(
            targetValue = state.progress,
            animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
            label = "driverProgress"
        )

        // Interpolated driver position
        val currentDriverX = startDriverX + (pickupX - startDriverX) * smoothProgress
        val currentDriverY = startDriverY + (pickupY - startDriverY) * smoothProgress

        // 1. Vector Polyline Route Path Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Upcoming Leg (Pickup -> Destination) as clean dashed route line
            val destPath = Path().apply {
                moveTo(pickupX, pickupY)
                cubicTo(
                    pickupX + 30f, (pickupY + destY) / 2f,
                    destX - 30f, (pickupY + destY) / 2f,
                    destX, destY
                )
            }

            // Outline Casing for upcoming route
            drawPath(
                path = destPath,
                color = NebengColor.Primary0,
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f))
                )
            )
            // Inner Core for upcoming route
            drawPath(
                path = destPath,
                color = NebengColor.Gray600,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f))
                )
            )

            // Draw Active Approach Leg (Driver Current Position -> Pickup)
            val approachPath = Path().apply {
                moveTo(currentDriverX, currentDriverY)
                lineTo(pickupX, pickupY)
            }

            // Outer dark route casing
            drawPath(
                path = approachPath,
                color = NebengColor.Primary900,
                style = Stroke(
                    width = 7.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            // Inner white guide track
            drawPath(
                path = approachPath,
                color = NebengColor.Primary0,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                )
            )

            // Ground Anchor Dot for Destination
            drawCircle(
                color = NebengColor.Primary900,
                radius = 5.dp.toPx(),
                center = Offset(destX, destY)
            )
            drawCircle(
                color = NebengColor.Primary0,
                radius = 2.dp.toPx(),
                center = Offset(destX, destY)
            )

            // Ground Anchor Dot for Pickup
            drawCircle(
                color = NebengColor.Primary900,
                radius = 6.dp.toPx(),
                center = Offset(pickupX, pickupY)
            )
            drawCircle(
                color = NebengColor.Primary0,
                radius = 2.5.dp.toPx(),
                center = Offset(pickupX, pickupY)
            )
        }

        // 2. Pointing Marker: Destination (SCBD Lot 8)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (destX - 85.dp.toPx()).roundToInt(),
                        y = (destY - 44.dp.toPx()).roundToInt()
                    )
                }
        ) {
            PointingPill(
                text = state.destinationLocation,
                icon = Icons.Default.Flag,
                isDark = true
            )
        }

        // 3. Pointing Marker: Pickup Location (Pintu Barat Lawson)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (pickupX - 105.dp.toPx()).roundToInt(),
                        y = (pickupY - 44.dp.toPx()).roundToInt()
                    )
                }
        ) {
            PointingPill(
                text = state.pickupLocation,
                icon = Icons.Default.Place,
                isDark = true,
                isCircleDot = true
            )
        }

        // 4. Pointing Marker: Driver Vehicle Position with Pulsing Radar Ripple
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (currentDriverX - 110.dp.toPx()).roundToInt(),
                        y = (currentDriverY - 48.dp.toPx()).roundToInt()
                    )
                }
        ) {
            DriverPointingMarker(
                driverName = state.driverName,
                vehicleModel = state.vehicleModel,
                vehiclePlate = state.vehiclePlate,
                vehicleType = state.vehicleType,
                bearing = state.driverBearing,
                isArrived = state.isArrived
            )
        }

        // 5. Floating Map Controls (Re-center & GPS Live Badge)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 175.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Live GPS Status Pill
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(NebengRadius.Full))
                    .clip(RoundedCornerShape(NebengRadius.Full))
                    .background(NebengColor.Primary0)
                    .border(1.dp, NebengColor.Gray200, RoundedCornerShape(NebengRadius.Full))
                    .clickable(onClick = onGpsBadgeClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "gpsLiveBlink")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "gpsDotAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(NebengColor.Danger600.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE GPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NebengColor.Primary900,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Re-center / Focus Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(NebengColor.Primary0)
                    .border(1.dp, NebengColor.Gray200, CircleShape)
                    .clickable(onClick = onRecenterClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Pusatkan Lokasi Driver",
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * High-contrast Pointing Marker with a sharp downward pointer needle (jarum pointer).
 */
@Composable
private fun PointingPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean = true,
    isCircleDot: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill Badge
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(NebengRadius.Full))
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(if (isDark) NebengColor.Primary900 else NebengColor.Primary0)
                .border(
                    width = if (isDark) 0.dp else 2.dp,
                    color = NebengColor.Primary900,
                    shape = RoundedCornerShape(NebengRadius.Full)
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isCircleDot) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isDark) NebengColor.Primary0 else NebengColor.Primary900)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDark) NebengColor.Primary0 else NebengColor.Primary900,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) NebengColor.Primary0 else NebengColor.Primary900,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }

        // Pointing Needle Anchor (Jarum Pointer Lancip Bawah)
        Canvas(modifier = Modifier.size(width = 12.dp, height = 7.dp)) {
            val needlePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(
                path = needlePath,
                color = if (isDark) NebengColor.Primary900 else NebengColor.Primary0
            )
            if (!isDark) {
                // Border for white needle
                val borderPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(size.width, 0f)
                }
                drawPath(
                    path = borderPath,
                    color = NebengColor.Primary900,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Driver Vehicle Pointing Marker with Animated Radar Pulse Ripple.
 */
@Composable
private fun DriverPointingMarker(
    driverName: String,
    vehicleModel: String,
    vehiclePlate: String,
    vehicleType: VehicleType,
    bearing: Float,
    isArrived: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")

    // Pulse Wave 1
    val wave1Radius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarWave1"
    )

    // Pulse Wave 2 (Staggered by 1000ms)
    val wave2Radius by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarWave2"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-contrast Driver Pill
        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(NebengRadius.Full))
                .clip(RoundedCornerShape(NebengRadius.Full))
                .background(NebengColor.Primary0)
                .border(
                    width = 2.dp,
                    color = if (isArrived) NebengColor.Primary900 else NebengColor.Primary900,
                    shape = RoundedCornerShape(NebengRadius.Full)
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Vehicle Icon
                Icon(
                    imageVector = if (vehicleType == VehicleType.MOTORCYCLE) {
                        Icons.Default.TwoWheeler
                    } else {
                        Icons.Default.DirectionsCar
                    },
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier.size(15.dp)
                )

                // Direction Needle
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = NebengColor.Primary900,
                    modifier = Modifier
                        .size(11.dp)
                        .rotate(bearing - 45f)
                )

                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$driverName (${vehicleModel.split(" ").firstOrNull() ?: ""} $vehiclePlate)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NebengColor.Primary900,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }

        // Pointing Needle Anchor
        Canvas(modifier = Modifier.size(width = 14.dp, height = 8.dp)) {
            val needlePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(path = needlePath, color = NebengColor.Primary0)

            val borderPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width / 2f, size.height)
                lineTo(size.width, 0f)
            }
            drawPath(
                path = borderPath,
                color = NebengColor.Primary900,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Pulsing Radar Ripple Canvas at road contact point
        Canvas(
            modifier = Modifier
                .size(48.dp)
                .offset(y = (-14).dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Radar Ring 1
            if (wave1Radius > 0f) {
                val r1 = 6.dp.toPx() + (20.dp.toPx() * wave1Radius)
                val alpha1 = (1f - wave1Radius).coerceIn(0f, 0.45f)
                drawCircle(
                    color = NebengColor.Primary900.copy(alpha = alpha1),
                    radius = r1,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Radar Ring 2
            if (wave2Radius > 0f) {
                val r2 = 6.dp.toPx() + (20.dp.toPx() * wave2Radius)
                val alpha2 = (1f - wave2Radius).coerceIn(0f, 0.45f)
                drawCircle(
                    color = NebengColor.Primary900.copy(alpha = alpha2),
                    radius = r2,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Vehicle Contact Point Dot
            drawCircle(
                color = NebengColor.Primary900,
                radius = 5.dp.toPx(),
                center = center
            )
            drawCircle(
                color = NebengColor.Primary0,
                radius = 2.dp.toPx(),
                center = center
            )
        }
    }
}
