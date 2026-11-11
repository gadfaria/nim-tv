package com.gadfaria.nimtv.data

import android.content.Context
import androidx.core.content.edit

/**
 * True when the last [limit] crashes all happened within [windowMs]: the app is
 * crash-looping, not just unlucky once.
 */
fun crashLooping(crashTimes: List<Long>, limit: Int, windowMs: Long): Boolean =
    crashTimes.size >= limit && crashTimes.last() - crashTimes[crashTimes.size - limit] <= windowMs

/**
 * Stops HomeRedirectService from trapping the TV in a crash loop.
 *
 * The service shares the app's process and reopens Nim TV whenever the stock
 * launcher shows. If Nim TV crashed on start, that would loop forever with the
 * Home key captured, and adb would be the only way out. After [LIMIT] crashes
 * in a row the guard trips and the service stands down, leaving Google TV's
 * launcher usable, until Nim TV proves stable again ([reset]).
 */
class CrashGuard(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("crash_guard", Context.MODE_PRIVATE)

    val tripped: Boolean
        get() = crashLooping(crashTimes(), LIMIT, WINDOW_MS)

    /** Called from the uncaught-exception handler, so it writes synchronously. */
    fun recordCrash(now: Long = System.currentTimeMillis()) {
        val times = (crashTimes() + now).takeLast(LIMIT)
        // commit, not apply: the process is about to die.
        prefs.edit(commit = true) { putString(KEY, times.joinToString(",")) }
    }

    fun reset() {
        if (prefs.contains(KEY)) prefs.edit { remove(KEY) }
    }

    private fun crashTimes(): List<Long> =
        prefs.getString(KEY, null)?.split(",")?.mapNotNull { it.toLongOrNull() }.orEmpty()

    private companion object {
        const val KEY = "crash_times"
        const val LIMIT = 3
        const val WINDOW_MS = 60_000L
    }
}
