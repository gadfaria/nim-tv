package com.gadfaria.nimtv.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings as AndroidSettings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gadfaria.nimtv.data.AppInfo
import com.gadfaria.nimtv.ui.theme.Foreground
import com.gadfaria.nimtv.ui.theme.MutedForeground
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    /** Null while loading. */
    favorites: List<AppInfo>?,
    movingKey: String?,
    homeRedirectRunning: Boolean,
    onLaunch: (AppInfo) -> Unit,
    onOpenMenu: (AppInfo) -> Unit,
    onMove: (Int) -> Unit,
    onStopMoving: () -> Unit,
    onOpenApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val firstItem = remember { FocusRequester() }

    // Keyed on emptiness, not on the list: reordering must not yank focus back
    // to the top while the user is moving a favorite. Nothing is focused while
    // loading, so focus doesn't jump from "Todos os apps" to the first favorite.
    val isEmpty = favorites?.isEmpty()
    LaunchedEffect(isEmpty) {
        if (isEmpty != null) runCatching { firstItem.requestFocus() }
    }

    // After a cold start the service binds a few seconds after the first frame;
    // waiting avoids flashing the hint when the Home button already works.
    var hintAllowed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(HINT_GRACE_MS)
        hintAllowed = true
    }

    BackHandler(enabled = movingKey != null, onBack = onStopMoving)

    // ~5% margins: many TVs crop the edges of the picture (overscan). Clock on
    // the left, favorites on the right: a column of both doesn't fit 16:9 once
    // there are more than a few favorites.
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 27.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.fillMaxHeight()) {
            Column(Modifier.padding(horizontal = 16.dp)) { Clock() }

            Spacer(Modifier.weight(1f))

            if (!homeRedirectRunning && hintAllowed) {
                TvItem(
                    text = "Ativar o botão Home em Acessibilidade",
                    style = MaterialTheme.typography.bodyLarge,
                    variant = TvItemVariant.Outline,
                    onClick = { openAccessibilitySettings(context) },
                    modifier = Modifier.width(CONTENT_WIDTH),
                )
                Spacer(Modifier.height(16.dp))
            }

            Signature()
        }

        // Full height whatever the number of favorites: "Todos os apps" always
        // sits at the bottom, like a sidebar footer.
        NimCard(Modifier.width(CONTENT_WIDTH).fillMaxHeight()) {
            GroupLabel("Favoritos")

            // Only the list scrolls: the label and "Todos os apps" stay in place.
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (favorites == null) {
                    SkeletonRows()
                } else if (favorites.isEmpty()) {
                    // CardDescription: what to do, instead of an empty card.
                    Text(
                        text = "Segure OK num app para favoritar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedForeground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                favorites?.forEachIndexed { index, app ->
                    // key() keeps the focused node attached to its app as the list reorders.
                    key(app.key) {
                        val moving = app.key == movingKey
                        TvItem(
                            text = if (moving) "↕  ${app.label}" else app.label,
                            onClick = { onLaunch(app) },
                            onLongClick = { onOpenMenu(app) },
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstItem) else Modifier)
                                .then(if (moving) Modifier.movingKeys(onMove, onStopMoving) else Modifier),
                        )
                    }
                }
            }

            Separator()

            TvItem(
                text = "Todos os apps",
                onClick = onOpenApps,
                muted = true,
                modifier = Modifier
                    // The system focuses the first focusable view on its own;
                    // while loading that would be this, then jump to a favorite.
                    .focusProperties { canFocus = isEmpty != null }
                    .then(if (isEmpty == true) Modifier.focusRequester(firstItem) else Modifier),
            )
        }
    }
}

private val CONTENT_WIDTH = 440.dp

private const val HINT_GRACE_MS = 10_000L

/** Author credit, quiet like a sidebar footer: muted, with the nick a step brighter. */
@Composable
private fun Signature() {
    Text(
        text = buildAnnotatedString {
            append("powered by ")
            withStyle(SpanStyle(color = Foreground, fontWeight = FontWeight.Medium)) {
                append("gadfaria")
            }
        },
        style = MaterialTheme.typography.labelMedium,
        color = MutedForeground,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

/**
 * Some TV builds (TCL on Android 11) don't handle the standard action and only
 * expose accessibility through the TV settings' OEM link.
 */
private fun openAccessibilitySettings(context: Context) {
    val actions = listOf(
        AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS,
        "android.settings.ACCESSIBILITY_TV_OEM_LINK",
    )
    actions.firstNotNullOfOrNull { action ->
        Intent(action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .takeIf { it.resolveActivity(context.packageManager) != null }
    }?.let { context.startActivity(it) }
}

/** While moving, up/down reorder instead of moving focus; OK confirms. */
private fun Modifier.movingKeys(onMove: (Int) -> Unit, onStop: () -> Unit) =
    onPreviewKeyEvent { event ->
        when (event.key) {
            Key.DirectionUp, Key.DirectionDown -> {
                if (event.type == KeyEventType.KeyDown) {
                    onMove(if (event.key == Key.DirectionUp) -1 else 1)
                }
                true
            }
            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                if (event.type == KeyEventType.KeyUp) onStop()
                true
            }
            else -> false
        }
    }

/**
 * Clock driven by ACTION_TIME_TICK (sent every minute): no wake-up loop, and
 * time zone or manual time changes come for free.
 */
@Composable
private fun rememberCurrentTime(): State<LocalDateTime> {
    val context = LocalContext.current
    val time = remember { mutableStateOf(LocalDateTime.now()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                time.value = LocalDateTime.now()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        time.value = LocalDateTime.now()
        onDispose { context.unregisterReceiver(receiver) }
    }
    return time
}

@Composable
private fun Clock() {
    val now by rememberCurrentTime()
    val context = LocalContext.current
    val locale = Locale.getDefault()

    // 12h/24h follows the system setting, like the system clock.
    val use24h = DateFormat.is24HourFormat(context)
    val hourFormat = remember(locale, use24h) {
        DateTimeFormatter.ofPattern(if (use24h) "HH:mm" else "h:mm", locale)
    }
    val dateFormat = remember(locale) { DateTimeFormatter.ofPattern("EEE, d MMM", locale) }

    Text(
        text = now.format(hourFormat),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Text(
        text = now.format(dateFormat),
        style = MaterialTheme.typography.bodyLarge,
        color = MutedForeground,
    )
}
