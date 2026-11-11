package com.gadfaria.nimtv

import android.app.Application
import com.gadfaria.nimtv.data.CrashGuard

class NimTvApplication : Application() {

    lateinit var crashGuard: CrashGuard
        private set

    override fun onCreate() {
        super.onCreate()
        crashGuard = CrashGuard(this)

        // Record the crash, then let the system handle it as usual.
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { crashGuard.recordCrash() }
            previous?.uncaughtException(thread, error)
        }
    }
}
