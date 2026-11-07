package com.gadfaria.nimtv

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * Makes the Home button open Nim TV.
 *
 * Google TV ignores third-party HOME activities: the button always opens its own
 * launcher. This service watches which app comes to the front and, when it's
 * the stock launcher, opens Nim TV over it. The cost is a brief flash of the
 * stock launcher.
 *
 * It only receives window-change events (see res/xml/home_redirect_service.xml),
 * never the contents of the screen.
 */
class HomeRedirectService : AccessibilityService() {

    private var stockLaunchers: Set<String> = KNOWN_STOCK_LAUNCHERS

    override fun onServiceConnected() {
        stockLaunchers = KNOWN_STOCK_LAUNCHERS + installedHomePackages()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() !in stockLaunchers) return

        // Accessibility services are exempt from background activity launch limits.
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    override fun onInterrupt() = Unit

    /**
     * Every app with a HOME activity, so a TCL-specific launcher is caught too.
     * Settings is left out: it owns the FallbackHome shown during boot.
     */
    private fun installedHomePackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.packageName }
            .filter { it != packageName && it !in IGNORED_HOME_PACKAGES }
            .toSet()
    }

    companion object {
        private val KNOWN_STOCK_LAUNCHERS = setOf(
            "com.google.android.apps.tv.launcherx", // Google TV
            "com.google.android.tvlauncher",        // Android TV
        )

        private val IGNORED_HOME_PACKAGES = setOf(
            "com.android.settings",
            "com.android.tv.settings",
        )

        fun isEnabled(context: Context): Boolean {
            val manager = context.getSystemService(AccessibilityManager::class.java)
            return manager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
        }
    }
}
