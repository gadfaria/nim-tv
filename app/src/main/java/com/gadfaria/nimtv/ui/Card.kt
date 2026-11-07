package com.gadfaria.nimtv.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gadfaria.nimtv.ui.theme.Background
import com.gadfaria.nimtv.ui.theme.Border
import com.gadfaria.nimtv.ui.theme.MutedForeground
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
