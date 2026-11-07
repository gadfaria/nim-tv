package com.gadfaria.nimtv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gadfaria.nimtv.R

// shadcn/ui "neutral" dark tokens, the same as hub's src/index.css (.dark),
// converted from oklch to sRGB.
val Background = Color(0xFF0A0A0A)      // --background  oklch(0.145 0 0)
val Foreground = Color(0xFFFAFAFA)      // --foreground  oklch(0.985 0 0)
val Accent = Color(0xFF262626)          // --accent / --muted / --secondary  oklch(0.269 0 0)
val MutedForeground = Color(0xFFA1A1A1) // --muted-foreground  oklch(0.708 0 0)
val Border = Color(0x1AFFFFFF)          // --border  oklch(1 0 0 / 10%)
val InputBorder = Color(0x26FFFFFF)     // --input  oklch(1 0 0 / 15%)
val Ring = Color(0xFF737373)            // --ring  oklch(0.556 0 0)
val Destructive = Color(0xFFFF6467)     // --destructive  oklch(0.704 0.191 22.216)

// --radius: 0.5rem, with shadcn's scale: md = 0.8x, lg = 1x, xl = 1.4x.
val RadiusMd = RoundedCornerShape(6.dp)
val RadiusLg = RoundedCornerShape(8.dp)
val RadiusXl = RoundedCornerShape(12.dp)

private val Geist = FontFamily(
    Font(R.font.geist_light, FontWeight.Light),
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
)

private val NimColors = darkColorScheme(
    background = Background,
    surface = Background,
    onBackground = Foreground,
    onSurface = Foreground,
    primary = Foreground,
    onPrimary = Background,
    error = Destructive,
)

// shadcn's type scale, enlarged for a TV read from ~3 m away.
val NimTypography = Typography(
    // The clock: the app's only display text.
    displayLarge = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Light,
        fontSize = 88.sp,
        letterSpacing = (-3).sp,
    ),
    // Screen and card titles: font-medium tracking-tight, like CardTitle.
    headlineMedium = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        letterSpacing = (-0.6).sp,
    ),
    // Menu and list items.
    titleMedium = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
    ),
    // Secondary text: the date, descriptions.
    bodyLarge = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
    ),
    // Group labels, like SidebarGroupLabel (text-xs font-medium, muted).
    labelMedium = TextStyle(
        fontFamily = Geist,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
    ),
)

/** Always dark: hub follows the system theme, but a TV launcher has no light mode. */
@Composable
fun NimTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NimColors, typography = NimTypography, content = content)
}
