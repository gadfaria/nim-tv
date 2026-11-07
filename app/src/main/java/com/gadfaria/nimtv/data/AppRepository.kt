package com.gadfaria.nimtv.data

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.text.Collator

/**
 * The TV's app list.
 *
 * PackageManager instead of LauncherApps (which the phone Nim uses):
 * LauncherApps only lists LAUNCHER activities, and TV apps register under
 * LEANBACK_LAUNCHER — Netflix, YouTube and Prime Video would be missing.
 */
class AppRepository(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    // Real alphabetical order: "Câmera" next to "Camera", not after "Z".
    private val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }

    /**
     * Emits the current list and re-emits on every install, uninstall or update.
     *
     * The receiver only signals; the list is read on IO. Reading every app's
     * label is slow on TV hardware and used to block the first frame.
     */
    val apps: Flow<List<AppInfo>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) = Unit.also { trySend(Unit) }
        }
        // Package broadcasts only arrive with the "package" data scheme set.
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        // EXPORTED: these broadcasts come from the system, not from this app.
        ContextCompat.registerReceiver(appContext, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        trySend(Unit)
        awaitClose { appContext.unregisterReceiver(receiver) }
    }
        // A burst of package events while a load runs collapses into one reload.
        .conflate()
        .map { load() }
        .flowOn(Dispatchers.IO)

    private fun load(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo }
            // Nim TV itself shows up in this list; opening it from itself is a no-op.
            .filter { it.packageName != appContext.packageName }
            .map { info ->
                AppInfo(
                    label = info.loadLabel(packageManager).toString(),
                    componentName = ComponentName(info.packageName, info.name),
                )
            }
            .sortedWith { a, b ->
                collator.compare(a.label, b.label).takeIf { it != 0 }
                    ?: a.componentName.compareTo(b.componentName)
            }
    }

    fun launch(app: AppInfo) {
        runCatching {
            val intent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                .setComponent(app.componentName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            appContext.startActivity(intent)
        }
    }

    fun openAppInfo(app: AppInfo) {
        runCatching {
            val intent = Intent(
                AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${app.componentName.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        }
    }

    /** Android's dialog confirms (and refuses, for a system app); nothing is deleted here. */
    fun requestUninstall(app: AppInfo) {
        runCatching {
            val intent = Intent(
                Intent.ACTION_DELETE,
                Uri.parse("package:${app.componentName.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(intent)
        }
    }
}
