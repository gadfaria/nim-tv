package com.gadfaria.nimtv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gadfaria.nimtv.data.AppInfo
import com.gadfaria.nimtv.data.AppRepository
import com.gadfaria.nimtv.data.Settings
import com.gadfaria.nimtv.data.moved
import com.gadfaria.nimtv.data.normalizeForSearch
import com.gadfaria.nimtv.data.rankBySearch
import com.gadfaria.nimtv.data.toggled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen { HOME, APPS }

class NimTvViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val settings = Settings(application)

    private val allApps: StateFlow<List<AppInfo>> = repository.apps.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList(),
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val visibleApps: StateFlow<List<AppInfo>> =
        combine(allApps, _query) { apps, query ->
            rankBySearch(apps, normalizeForSearch(query.trim())) { it.searchable }.items
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Favorites resolved to apps, in the stored order. A key with no matching
     * app is ignored, not deleted: the app may be mid-update.
     */
    val favorites: StateFlow<List<AppInfo>> =
        combine(allApps, settings.favoriteKeys) { apps, keys ->
            val byKey = apps.associateBy { it.key }
            keys.mapNotNull { byKey[it] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteKeys: StateFlow<Set<String>> = settings.favoriteKeys
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _screen = MutableStateFlow(Screen.HOME)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    /** The app whose menu is open, if any. */
    private val _menuApp = MutableStateFlow<AppInfo?>(null)
    val menuApp: StateFlow<AppInfo?> = _menuApp.asStateFlow()

    /** The favorite being reordered with up/down on the home, if any. */
    private val _movingKey = MutableStateFlow<String?>(null)
    val movingKey: StateFlow<String?> = _movingKey.asStateFlow()

    private val _homeRedirectEnabled = MutableStateFlow(true)
    val homeRedirectEnabled: StateFlow<Boolean> = _homeRedirectEnabled.asStateFlow()

    fun refreshHomeRedirect() {
        _homeRedirectEnabled.value = HomeRedirectService.isEnabled(getApplication())
    }

    fun openApps() { _screen.value = Screen.APPS }

    /** Going home always clears the search and closes any menu. */
    fun goHome() {
        _screen.value = Screen.HOME
        _query.value = ""
        _menuApp.value = null
        _movingKey.value = null
    }

    fun onQueryChange(value: String) { _query.value = value }

    fun openMenu(app: AppInfo) { _menuApp.value = app }

    fun closeMenu() { _menuApp.value = null }

    fun toggleFavorite(app: AppInfo) {
        viewModelScope.launch { settings.updateFavorites { it.toggled(app.key) } }
    }

    fun startMoving(app: AppInfo) { _movingKey.value = app.key }

    fun stopMoving() { _movingKey.value = null }

    fun moveMovingFavorite(delta: Int) {
        val key = _movingKey.value ?: return
        val visible = allApps.value.mapTo(HashSet()) { it.key }
        viewModelScope.launch { settings.updateFavorites { it.moved(key, delta, visible) } }
    }

    fun launch(app: AppInfo) = repository.launch(app)

    fun openAppInfo(app: AppInfo) = repository.openAppInfo(app)

    fun requestUninstall(app: AppInfo) = repository.requestUninstall(app)
}
