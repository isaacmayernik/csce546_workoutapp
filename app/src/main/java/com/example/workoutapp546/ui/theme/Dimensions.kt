package com.example.workoutapp546.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Dimensions {
    val bodyTextSize = 12.sp
    val buttonTextSize = 12.sp

    @Composable
    fun scaledSp(baseSp: TextUnit): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val density = configuration.densityDpi

        val baseWidth = 360.dp
        val baseDensity = 320

        val widthFactor = screenWidth / baseWidth
        val densityFactor = density.toFloat() / baseDensity
        val scaleFactor = minOf(widthFactor, densityFactor)

        return baseSp * maxOf(scaleFactor, 1.0f)
    }
}