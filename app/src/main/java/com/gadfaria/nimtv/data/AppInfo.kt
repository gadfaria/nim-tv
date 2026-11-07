package com.gadfaria.nimtv.data

import android.content.ComponentName
import java.text.Normalizer
import java.util.Locale

/**
 * A launchable TV app. No icon or banner on purpose: the launcher is text only.
 *
 * No user/profile field: TVs have no work profile.
 */
data class AppInfo(
    val label: String,
    val componentName: ComponentName,
) {
    /** Identity across restarts — what goes to disk in the favorites list. */
    val key: String = componentName.flattenToShortString()

    /** Prepared once at load time, not on every keystroke. */
    val searchable: SearchableLabel = searchableOf(label)
}

/**
 * Lowercase and accent-free, so "cam" finds "Câmera".
 *
 * NFD splits the letter from the accent; dropping the diacritics (Unicode
 * category Mn) leaves the base letter.
 */
fun normalizeForSearch(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase(Locale.getDefault())

private val DIACRITICS = "\\p{Mn}+".toRegex()
