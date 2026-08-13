package de.unixkiwi.betterschool.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NavbarScreen(modifier: Modifier = Modifier) {
    /*    val navItems = listOf(
            MainNavItem.Timetable,
            MainNavItem.Grades,
            MainNavItem.Settings
        )
        val navController = rememberNavController()
        var selectedDestination by rememberSaveable { mutableIntStateOf(MainNavItem.Timetable.navIndex) }

        Scaffold(
            modifier = modifier,
            bottomBar = {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {

                    navItems.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = selectedDestination == index,
                            onClick = {
                                navController.navigate(route = destination.screen.route)
                                selectedDestination = index
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = if (selectedDestination == index) destination.selectedIcon else destination.unselectedIcon),
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(destination.label)
                            }
                        )
                    }
                }

            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Timetable.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Timetable.route) { TimetableScreen() }
                composable(Screen.Grades.route) { GradesScreen() }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }*/
}