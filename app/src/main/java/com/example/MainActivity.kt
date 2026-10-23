package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ResonanceMusicTheme
import com.example.ui.theme.getThemeAccentColor
import com.example.ui.viewmodel.MusicPlayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MusicPlayerViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val themeAccentColor = getThemeAccentColor(uiState.themeAccent)

            ResonanceMusicTheme(accentColor = themeAccentColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
