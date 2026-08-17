package de.unixkiwi.betterschool.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import de.unixkiwi.betterschool.R
import de.unixkiwi.betterschool.core.components.MenuButton
import de.unixkiwi.betterschool.core.models.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onMenuBtnClicked: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    MenuButton(onMenuBtnClicked)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle("Appearance")

            val appearanceItems = listOf<@Composable (Int, Int) -> Unit>(
                { index, count ->
                    SettingsListItem(
                        title = "Dynamic Colors",
                        subtitle = "Use system accent colors",
                        trailingContent = {
                            Switch(
                                checked = settings.useDynamicColors,
                                onCheckedChange = { viewModel.setUseDynamicColors(it) }
                            )
                        },
                        onClick = { viewModel.setUseDynamicColors(!settings.useDynamicColors) },
                        index = index,
                        count = count
                    )
                },
                { index, count ->
                    SettingsListItem(
                        title = when (settings.theme) {
                            AppTheme.SYSTEM -> "System default"
                            AppTheme.LIGHT -> "Light"
                            AppTheme.DARK -> "Dark"
                        },
                        onClick = {
                            when (settings.theme) {
                                AppTheme.SYSTEM -> viewModel.setTheme(AppTheme.DARK)
                                AppTheme.LIGHT -> viewModel.setTheme(AppTheme.SYSTEM)
                                AppTheme.DARK -> viewModel.setTheme(AppTheme.LIGHT)
                            }
                        },
                        trailingContent = {
                            ThemeSwitcher(
                                theme = settings.theme,
                                onClick = { viewModel.setTheme(it) }
                            )
                        },
                        index = index,
                        count = count
                    )
                }
            )

            appearanceItems.forEachIndexed { index, item ->
                item(index, appearanceItems.size)
                Spacer(Modifier.height(ListItemDefaults.SegmentedGap))
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsListItem(
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    index: Int,
    count: Int
) {
    ListItem(
        onClick = onClick,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shapes = if (count == 1) {
            ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
        } else {
            ListItemDefaults.segmentedShapes(
                index = index,
                count = count
            )
        },
        leadingContent = icon,
        trailingContent = trailingContent,
        supportingContent = subtitle?.let { { Text(it) } },
        content = { Text(title) }
    )
}

@Composable
fun ThemeSwitcher(
    theme: AppTheme,
    onClick: (AppTheme) -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
    ) {
        IconToggleButton(
            onCheckedChange = { onClick(AppTheme.LIGHT) },
            checked = theme == AppTheme.LIGHT,
            modifier = if (theme == AppTheme.LIGHT) Modifier.background(
                MaterialTheme.colorScheme.primaryContainer,
                CircleShape
            ) else Modifier
        ) {
            Icon(painterResource(R.drawable.light_mode_24px), null)
        }
        IconToggleButton(
            onCheckedChange = { onClick(AppTheme.SYSTEM) },
            checked = theme == AppTheme.SYSTEM,
            modifier = if (theme == AppTheme.SYSTEM) Modifier.background(
                MaterialTheme.colorScheme.primaryContainer,
                CircleShape
            ) else Modifier
        ) {
            Icon(painterResource(R.drawable.brightness_auto_24px), null)
        }
        IconToggleButton(
            onCheckedChange = { onClick(AppTheme.DARK) },
            checked = theme == AppTheme.DARK,
            modifier = if (theme == AppTheme.DARK) Modifier.background(
                MaterialTheme.colorScheme.primaryContainer,
                CircleShape
            ) else Modifier
        ) {
            Icon(painterResource(R.drawable.dark_mode_24px), null)
        }
    }
}
