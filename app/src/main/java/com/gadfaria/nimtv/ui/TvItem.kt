package com.gadfaria.nimtv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gadfaria.nimtv.ui.theme.NimBackground
import com.gadfaria.nimtv.ui.theme.NimForeground
import com.gadfaria.nimtv.ui.theme.NimForegroundMuted

private val SELECT_KEYS = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)

/**
 * A focusable line of text: the only interactive element in Nim TV.
 *
 * Focus is shown by inverting the two colors — readable from the couch without
 * adding a third color.
 *
 * Holding OK (or pressing the remote's Menu key) is the TV's long press. The
 * click fires on key UP, and only if this item also saw the key DOWN: otherwise
 * the release of the long press that opened a menu would click the menu's first
 * item.
 */
@Composable
fun TvItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    muted: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var longPressFired by remember { mutableStateOf(false) }

    Text(
        text = text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = when {
            focused -> NimBackground
            muted -> NimForegroundMuted
            else -> NimForeground
        },
        modifier = modifier
            .onFocusChanged {
                focused = it.isFocused
                if (!it.isFocused) pressed = false
            }
            .onKeyEvent { event ->
                when {
                    event.key in SELECT_KEYS -> {
                        if (event.type == KeyEventType.KeyDown) {
                            // Held keys repeat after ~500 ms: the first repeat is the long press.
                            if (event.nativeKeyEvent.repeatCount == 0) {
                                pressed = true
                                longPressFired = false
                            } else if (pressed && !longPressFired && onLongClick != null) {
                                longPressFired = true
                                onLongClick()
                            }
                        } else if (event.type == KeyEventType.KeyUp) {
                            if (pressed && !longPressFired) onClick()
                            pressed = false
                        }
                        true
                    }
                    event.key == Key.Menu && onLongClick != null -> {
                        if (event.type == KeyEventType.KeyUp) onLongClick()
                        true
                    }
                    else -> false
                }
            }
            .focusable()
            .background(
                color = if (focused) NimForeground else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
    )
}
