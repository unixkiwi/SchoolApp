package de.unixkiwi.betterschool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.unixkiwi.betterschool.core.models.AppTheme
import de.unixkiwi.betterschool.ui.RootNavigation
import de.unixkiwi.betterschool.ui.settings.SettingsViewModel
import de.unixkiwi.betterschool.ui.theme.BetterSchoolTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        enableEdgeToEdge()

        setContent {
            val settings by settingsViewModel.settingsState.collectAsStateWithLifecycle()
            val darkTheme = when (settings.theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            BetterSchoolTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.useDynamicColors
            ) {
                val navController = rememberNavController()

                RootNavigation(navController)
            }
        }
    }
}

