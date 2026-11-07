package com.gadfaria.nimtv

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Makes the Home button open Nim TV.
 *
 * Google TV ignores third-party HOME activities: the button always opens its own
 * launcher. Two paths cover it:
 *
 * 1. The Home key is consumed here, before the system sees it. Letting the
 *    system handle it and reacting afterwards fails on Android 11: after a Home
 *    press, activity starts from other apps are held back for ~5 s.
 * 2. When the stock launcher still comes to the front another way (Back out of
 *    an app, boot), Nim TV is opened over it — with a brief flash.
 *
 * It only receives window-change and key events (see
 * res/xml/home_redirect_service.xml), never the contents of the screen.
 */
class HomeRedirectService : AccessibilityService() {

    private var stockLaunchers: Set<String> = KNOWN_STOCK_LAUNCHERS

    /** One window change fires several events; one launch is enough. */
    private var lastLaunchAt = 0L

    override fun onServiceConnected() {
        stockLaunchers = KNOWN_STOCK_LAUNCHERS + installedHomePackages()
        _running.value = true
    }

    override fun onDestroy() {
        _running.value = false
        super.onDestroy()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_HOME) return false
        // Down and up are both consumed, so the system never runs its Home action.
        if (event.action == KeyEvent.ACTION_UP) openNim()
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() !in stockLaunchers) return
        val now = SystemClock.uptimeMillis()
        if (now - lastLaunchAt < DEBOUNCE_MS) return
        lastLaunchAt = now
        openNim()
    }

    // Accessibility services are exempt from background activity launch limits.
    private fun openNim() {
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    override fun onInterrupt() = Unit

    /**
     * Every app with a HOME activity, so a TCL-specific launcher is caught too.
     * System packages in [IGNORED_HOME_PACKAGES] are left out.
     */
    private fun installedHomePackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
            .filter { it != packageName && it !in IGNORED_HOME_PACKAGES }
            .toSet()
    }

    companion object {
        private const val DEBOUNCE_MS = 1_000L

        private val KNOWN_STOCK_LAUNCHERS = setOf(
            "com.google.android.apps.tv.launcherx", // Google TV
            "com.google.android.tvlauncher",        // Android TV
        )

        // Settings owns FallbackHome (shown during boot); setupwraith owns Google
        // TV's setup and recovery screens. Bouncing either would break them.
        private val IGNORED_HOME_PACKAGES = setOf(
            "com.android.settings",
            "com.android.tv.settings",
            "com.google.android.tungsten.setupwraith",
        )

        private val _running = MutableStateFlow(false)

        /**
         * Whether the service is actually running, not just switched on: TCL
         * can leave an enabled service unbound (no auto-start permission), and
         * rebinds it only seconds after an app update. Same process as the
         * activity, so a static flow is enough.
         */
        val running: StateFlow<Boolean> = _running.asStateFlow()
    }
}
