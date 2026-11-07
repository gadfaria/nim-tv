package com.gadfaria.nimtv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gadfaria.nimtv.data.AppInfo
import com.gadfaria.nimtv.ui.theme.NimForeground
import com.gadfaria.nimtv.ui.theme.NimForegroundMuted
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
        SearchField(query = query, onQueryChange = onQueryChange)

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

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(color = NimForeground),
        cursorBrush = SolidColor(NimForeground),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // "Search" on the TV keyboard jumps straight to the results.
        keyboardActions = KeyboardActions(onSearch = { focusManager.moveFocus(FocusDirection.Down) }),
        modifier = Modifier
            .width(560.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (focused) NimForeground.copy(alpha = 0.12f) else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "buscar",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NimForegroundMuted,
                    )
                }
                inner()
            }
        },
    )
}
