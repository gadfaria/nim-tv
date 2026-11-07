package com.gadfaria.nimtv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gadfaria.nimtv.data.AppInfo
import com.gadfaria.nimtv.ui.theme.Foreground
import com.gadfaria.nimtv.ui.theme.InputBorder
import com.gadfaria.nimtv.ui.theme.MutedForeground
import com.gadfaria.nimtv.ui.theme.RadiusLg
import com.gadfaria.nimtv.ui.theme.Ring
import kotlinx.coroutines.flow.first

/**
 * Every app, A–Z, in a grid: a wide screen fits four columns of names, and the
 * D-pad moves through it in both directions.
 */
@Composable
fun AppsScreen(
    apps: List<AppInfo>,
    favoriteKeys: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onLaunch: (AppInfo) -> Unit,
    onOpenMenu: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstApp = remember { FocusRequester() }
    val gridState = rememberLazyGridState()

    // Focus lands on the first app, not on the search: typing on a remote is the
    // slow path, kept one press up. Lazy items only exist after the grid's first
    // layout, so requesting focus right away silently does nothing.
    LaunchedEffect(Unit) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.isNotEmpty() }.first { it }
        runCatching { firstApp.requestFocus() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 27.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Apps",
                style = MaterialTheme.typography.headlineMedium,
                color = Foreground,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.width(16.dp))
            SearchField(query = query, onQueryChange = onQueryChange)
        }

        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(apps, key = { _, app -> app.key }) { index, app ->
                TvItem(
                    text = if (app.key in favoriteKeys) "${app.label}  ·" else app.label,
                    onClick = { onLaunch(app) },
                    onLongClick = { onOpenMenu(app) },
                    modifier = if (index == 0) Modifier.focusRequester(firstApp) else Modifier,
                )
            }
        }
    }
}

/**
 * shadcn's Input: rounded-lg, 1dp input border over bg-input/30; when focused
 * the border turns into the ring color.
 */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }
    val style = MaterialTheme.typography.titleMedium

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = style.copy(color = Foreground),
        cursorBrush = SolidColor(Foreground),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // "Search" on the TV keyboard jumps straight to the results.
        keyboardActions = KeyboardActions(onSearch = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier
            .width(440.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .border(if (focused) 2.dp else 1.dp, if (focused) Ring else InputBorder, RadiusLg)
                    .background(InputBorder.copy(alpha = 0.045f), RadiusLg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                if (query.isEmpty()) {
                    Text(text = "Buscar apps…", style = style, color = MutedForeground)
                }
                inner()
            }
        },
    )
}
