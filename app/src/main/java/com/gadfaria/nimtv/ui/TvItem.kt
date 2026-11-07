package com.gadfaria.nimtv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gadfaria.nimtv.ui.theme.Accent
import com.gadfaria.nimtv.ui.theme.Destructive
import com.gadfaria.nimtv.ui.theme.Foreground
import com.gadfaria.nimtv.ui.theme.InputBorder
import com.gadfaria.nimtv.ui.theme.MutedForeground
import com.gadfaria.nimtv.ui.theme.RadiusLg
import com.gadfaria.nimtv.ui.theme.RadiusMd
import com.gadfaria.nimtv.ui.theme.Ring

/** The look of a [TvItem], named after the shadcn component it mirrors. */
enum class TvItemVariant {
    /** SidebarMenuButton: plain text, accent background when focused. */
    Menu,

    /** Button variant="outline": always framed, for calls to action. */
    Outline,
}

private val SELECT_KEYS = setOf(Key.DirectionCenter, Key.Enter, Key.NumPadEnter)

/**
 * A focusable line of text: the only interactive element in Nim TV.
 *
 * Focus looks like hub's active sidebar item (accent background, medium weight)
 * plus a 2dp ring, so it reads from the couch.
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
    variant: TvItemVariant = TvItemVariant.Menu,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    muted: Boolean = false,
    destructive: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var longPressFired by remember { mutableStateOf(false) }

    val shape = if (variant == TvItemVariant.Outline) RadiusLg else RadiusMd
    val borderColor = when {
        focused -> Ring
        variant == TvItemVariant.Outline -> InputBorder
        else -> Color.Transparent
    }
    val borderWidth = if (focused) 2.dp else 1.dp
    val background = when {
        // focus:bg-destructive/10, as in shadcn's destructive menu item.
        focused && destructive -> Destructive.copy(alpha = 0.1f)
        focused -> Accent
        // dark:bg-input/30 on shadcn's outline button.
        variant == TvItemVariant.Outline -> InputBorder.copy(alpha = 0.045f)
        else -> Color.Transparent
    }

    Text(
        text = text,
        style = style,
        fontWeight = if (focused) FontWeight.Medium else null,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = when {
            destructive -> Destructive
            focused -> Foreground
            muted -> MutedForeground
            else -> Foreground
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
            .fillMaxWidth()
            .border(borderWidth, borderColor, shape)
            .background(background, shape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
