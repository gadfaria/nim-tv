package com.gadfaria.nimtv.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gadfaria.nimtv.ui.theme.Accent
import com.gadfaria.nimtv.ui.theme.Background
import com.gadfaria.nimtv.ui.theme.Border
import com.gadfaria.nimtv.ui.theme.MutedForeground
import com.gadfaria.nimtv.ui.theme.RadiusMd
import com.gadfaria.nimtv.ui.theme.RadiusXl

/** shadcn's Card: rounded-xl, bg-card, ring-1 ring-foreground/10. */
@Composable
fun NimCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .background(Background, RadiusXl)
            .border(1.dp, Border, RadiusXl)
            .padding(8.dp),
        content = content,
    )
}

/** SidebarGroupLabel: small, medium weight, muted. */
@Composable
fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MutedForeground,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** shadcn's Separator: a 1dp line in the border color. */
@Composable
fun Separator(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
            .background(Border),
    )
}

// Uneven widths read as text lines rather than a progress bar.
private val SKELETON_WIDTHS = listOf(0.55f, 0.4f, 0.48f)

/**
 * shadcn's Skeleton: accent blocks with a slow pulse, shaped like the rows
 * they stand in for (same height as a [TvItem]).
 */
@Composable
fun SkeletonRows(modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "pulse",
    )
    Column(modifier) {
        SKELETON_WIDTHS.forEach { width ->
            Box(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(width)
                    .height(20.dp)
                    .graphicsLayer { alpha = pulse }
                    .background(Accent, RadiusMd),
            )
        }
    }
}
