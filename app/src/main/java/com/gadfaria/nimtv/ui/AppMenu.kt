package com.gadfaria.nimtv.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.gadfaria.nimtv.ui.theme.Foreground

/**
 * Long-press menu, shaped like shadcn's DialogContent (a card with a title).
 * "Mover" only exists on the home, where the order is visible.
 */
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
        NimCard(Modifier.width(400.dp)) {
            // DialogTitle: font-medium, one step above the items.
            Text(
                text = app.label,
                style = MaterialTheme.typography.headlineMedium,
                color = Foreground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Spacer(Modifier.height(4.dp))
            TvItem(
                text = if (isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                onClick = act(onToggleFavorite),
                modifier = Modifier.focusRequester(firstItem),
            )
            if (isFavorite && canMove) {
                TvItem(text = "Mover", onClick = act(onMove))
            }
            TvItem(text = "Informações do app", onClick = act(onAppInfo))
            // The destructive action sits apart, as in a DropdownMenu.
            Separator()
            TvItem(text = "Desinstalar", onClick = act(onUninstall), destructive = true)
        }
    }
}
