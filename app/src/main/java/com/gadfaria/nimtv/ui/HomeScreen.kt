package com.gadfaria.nimtv.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings as AndroidSettings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gadfaria.nimtv.data.AppInfo
import com.gadfaria.nimtv.ui.theme.NimForegroundMuted
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    favorites: List<AppInfo>,
    movingKey: String?,
    homeRedirectEnabled: Boolean,
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
    // to the top while the user is moving a favorite.
    LaunchedEffect(favorites.isEmpty()) {
        runCatching { firstItem.requestFocus() }
    }

    BackHandler(enabled = movingKey != null, onBack = onStopMoving)

    // ~5% margins: many TVs crop the edges of the picture (overscan).
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 27.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) { Clock() }

        Spacer(Modifier.height(40.dp))

        favorites.forEachIndexed { index, app ->
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

        TvItem(
            text = "todos os apps",
            onClick = onOpenApps,
            muted = true,
            modifier = if (favorites.isEmpty()) Modifier.focusRequester(firstItem) else Modifier,
        )

        if (!homeRedirectEnabled) {
            Spacer(Modifier.height(40.dp))
            TvItem(
                text = "Ativar o botão Home em Acessibilidade",
                style = MaterialTheme.typography.bodyLarge,
                muted = true,
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }
    }
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
        color = NimForegroundMuted,
    )
}
