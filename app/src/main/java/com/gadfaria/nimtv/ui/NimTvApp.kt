package com.gadfaria.nimtv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gadfaria.nimtv.NimTvViewModel
import com.gadfaria.nimtv.Screen

@Composable
fun NimTvApp(viewModel: NimTvViewModel) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteKeys by viewModel.favoriteKeys.collectAsStateWithLifecycle()
    val apps by viewModel.visibleApps.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val menuApp by viewModel.menuApp.collectAsStateWithLifecycle()
    val movingKey by viewModel.movingKey.collectAsStateWithLifecycle()
    val homeRedirectEnabled by viewModel.homeRedirectEnabled.collectAsStateWithLifecycle()

    when (screen) {
        Screen.HOME -> HomeScreen(
            favorites = favorites,
            movingKey = movingKey,
            homeRedirectEnabled = homeRedirectEnabled,
            onLaunch = viewModel::launch,
            onOpenMenu = viewModel::openMenu,
            onMove = viewModel::moveMovingFavorite,
            onStopMoving = viewModel::stopMoving,
            onOpenApps = viewModel::openApps,
        )
        Screen.APPS -> {
            BackHandler(onBack = viewModel::goHome)
            AppsScreen(
                apps = apps,
                favoriteKeys = favoriteKeys,
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onLaunch = viewModel::launch,
                onOpenMenu = viewModel::openMenu,
            )
        }
    }

    menuApp?.let { app ->
        AppMenu(
            app = app,
            isFavorite = app.key in favoriteKeys,
            canMove = screen == Screen.HOME,
            onDismiss = viewModel::closeMenu,
            onToggleFavorite = { viewModel.toggleFavorite(app) },
            onMove = { viewModel.startMoving(app) },
            onAppInfo = { viewModel.openAppInfo(app) },
            onUninstall = { viewModel.requestUninstall(app) },
        )
    }
}
