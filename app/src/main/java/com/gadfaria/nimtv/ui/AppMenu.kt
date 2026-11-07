package com.gadfaria.nimtv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gadfaria.nimtv.data.AppInfo
import com.gadfaria.nimtv.ui.theme.NimBackground
import com.gadfaria.nimtv.ui.theme.NimForegroundMuted

/** Long-press menu. "Mover" only exists on the home, where the order is visible. */
@Composable
fun AppMenu(
    app: AppInfo,
    isFavorite: Boolean,
    canMove: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMove: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
) {
    val firstItem = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstItem.requestFocus() } }

    // Every action closes the menu first, so focus goes back to the screen.
    fun act(action: () -> Unit): () -> Unit = { onDismiss(); action() }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .widthIn(min = 360.dp)
                .background(NimBackground, RoundedCornerShape(12.dp))
                .border(1.dp, NimForegroundMuted, RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                color = NimForegroundMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            TvItem(
                text = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                onClick = act(onToggleFavorite),
                modifier = Modifier.focusRequester(firstItem),
            )
            if (isFavorite && canMove) {
                TvItem(text = "Mover", onClick = act(onMove))
            }
            TvItem(text = "Informações do app", onClick = act(onAppInfo))
            TvItem(text = "Desinstalar", onClick = act(onUninstall))
        }
    }
}
