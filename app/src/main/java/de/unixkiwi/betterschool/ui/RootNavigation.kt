package de.unixkiwi.betterschool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.unixkiwi.betterschool.core.navigation.Screen
import de.unixkiwi.betterschool.ui.auth.AuthScreen
import de.unixkiwi.betterschool.ui.settings.SettingsScreen
import de.unixkiwi.betterschool.ui.timetable.TimetableScreen
import kotlinx.coroutines.launch

@Composable
fun RootNavigation(navController: NavHostController) {
    var destination by rememberSaveable { mutableStateOf<String>(Screen.Auth.route) }

    // Nav Drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "BetterSchool",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = { Text("Timetable") },
                        selected = destination == Screen.Timetable.route,
                        onClick = { /* Handle click */ }
                    )
                    NavigationDrawerItem(
                        label = { Text("Grades") },
                        selected = destination == Screen.Grades.route,
                        onClick = { /* Handle click */ }
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = destination == Screen.Settings.route,
                        onClick = { /* Handle click */ }
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        NavHost(navController = navController, startDestination = destination) {
            composable(Screen.Auth.route) {
                AuthScreen(onSuccessfulLogin = {
                    destination = Screen.Timetable.route
                    navController.navigate(Screen.Timetable.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Timetable.route) {
                TimetableScreen(
                    onMenuBtnClicked = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                    onLoginBtnClick = {
                        destination = Screen.Auth.route
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Timetable.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}