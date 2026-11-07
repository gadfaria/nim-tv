package com.gadfaria.nimtv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.gadfaria.nimtv.ui.NimTvApp
import com.gadfaria.nimtv.ui.theme.Background
import com.gadfaria.nimtv.ui.theme.NimTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NimTvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On a home, back does nothing: finishing would reveal the stock launcher,
        // which HomeRedirectService would then bounce straight back to Nim TV.
        // The apps screen's BackHandler is registered later and takes priority.
        onBackPressedDispatcher.addCallback(this) { /* intentional no-op */ }

        setContent {
            NimTheme {
                Box(Modifier.fillMaxSize().background(Background)) {
                    NimTvApp(viewModel)
                }
            }
        }
    }

    /** HOME (or the redirect service) while Nim TV is already open. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.goHome()
    }
}
