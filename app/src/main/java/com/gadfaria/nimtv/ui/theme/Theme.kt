package com.gadfaria.nimtv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The same two colors as the phone Nim.
val NimBackground = Color(0xFF121212)
val NimForeground = Color(0xFFE8E6E3)

// Same color with less presence: hierarchy without a third color.
val NimForegroundMuted = Color(0x99E8E6E3)

private val NimColors = darkColorScheme(
    background = NimBackground,
    surface = NimBackground,
    onBackground = NimForeground,
    onSurface = NimForeground,
    primary = NimForeground,
)

// Larger than the phone: the TV is read from the couch, ~3 m away.
val NimTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 88.sp,
        letterSpacing = (-3).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
    ),
)

/** Always dark: the background is the launcher's decision, not a device preference. */
@Composable
fun NimTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NimColors, typography = NimTypography, content = content)
}
