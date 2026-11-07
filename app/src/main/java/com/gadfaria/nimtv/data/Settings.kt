package com.gadfaria.nimtv.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nim_tv")

/** Launcher preferences. Only favorites, stored as an ordered list. */
class Settings(private val context: Context) {

    // A list, not a Set: the order is what the home shows. `\n` is safe because
    // a flattened ComponentName never contains a line break.
    private val favoritesKey = stringPreferencesKey("favorites")

    val favoriteKeys: Flow<List<String>> = context.dataStore.data.map { it.favorites() }

    suspend fun updateFavorites(transform: (List<String>) -> List<String>) {
        context.dataStore.edit { prefs ->
            prefs[favoritesKey] = transform(prefs.favorites()).joinToString(SEPARATOR)
        }
    }

    private fun Preferences.favorites(): List<String> =
        this[favoritesKey]?.split(SEPARATOR)?.filter { it.isNotEmpty() }.orEmpty()

    private companion object {
        const val SEPARATOR = "\n"
    }
}
