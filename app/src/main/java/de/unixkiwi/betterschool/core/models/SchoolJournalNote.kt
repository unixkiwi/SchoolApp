package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolJournalNote(
    val description: String = "No Content",
    val type: String = "Unknown Type"
)
