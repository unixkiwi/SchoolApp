package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolSubject(
    val shortName: String = "---",
    val name: String = "Unknown Subject",
)
