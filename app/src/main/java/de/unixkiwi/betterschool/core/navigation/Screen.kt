package de.unixkiwi.betterschool.core.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Timetable : Screen("timetable")
    object Grades : Screen("grades")
    object Settings : Screen("settings")
}