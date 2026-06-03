package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
enum class SchoolLessonStatus(val apiValue: String) {
    INITIAL("initial"),
    PLANNED("planned"),
    CANCELED("canceled"),
    HOLD("hold");

    companion object {
        fun fromApiValue(value: String): SchoolLessonStatus {
            return entries.firstOrNull { it.apiValue == value.lowercase() } ?: INITIAL
        }
    }
}
