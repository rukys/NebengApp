package com.disinidev.nebeng.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object NebengRadius {
    val Full = 999.dp
    val Xxl = 24.dp
    val Xl = 20.dp
    val Lg = 16.dp
    val Md = 12.dp
    val Sm = 8.dp
    val Xs = 4.dp
}

object NebengSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 20.dp
    val Xxl = 24.dp
    val Xxxl = 32.dp
}

val NebengShapes = Shapes(
    extraSmall = RoundedCornerShape(NebengRadius.Xs),
    small = RoundedCornerShape(NebengRadius.Sm),
    medium = RoundedCornerShape(NebengRadius.Md),
    large = RoundedCornerShape(NebengRadius.Lg),
    extraLarge = RoundedCornerShape(NebengRadius.Xxl)
)
