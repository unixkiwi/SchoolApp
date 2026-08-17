package de.unixkiwi.betterschool.core.models

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

data class Settings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val useDynamicColors: Boolean = true
)
