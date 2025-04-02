package com.example.workoutapp546.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimensions {
    val bodyTextSize = 10.sp
    val buttonTextSize = 10.sp

    val buttonHeight = 50.dp
    val buttonWidth = 90.dp
    val buttonPadding = 8.dp

    @Composable
    fun scaledSp(baseSp: TextUnit): TextUnit {
        val configuration = LocalConfiguration.current
        val density = configuration.densityDpi
        val baseDensity = 320

        val densityFactor = density.toFloat() / baseDensity
        val additionalHighDpiBoost = if (density >= 420) 1.6f / 1.3125f else 1f
        val scaleFactor = densityFactor * additionalHighDpiBoost

        return baseSp * maxOf(scaleFactor, 1.0f)
    }

    @Composable
    fun scaledDp(baseDp: Dp): Dp {
        val configuration = LocalConfiguration.current
        val density = configuration.densityDpi
        val baseDensity = 320

        val densityFactor = density.toFloat() / baseDensity
        val scaleFactor = minOf(densityFactor * 1.1f, 1.4f)

        return baseDp * maxOf(scaleFactor, 1.0f)
    }
}