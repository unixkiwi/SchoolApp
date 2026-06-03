package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolTeacher(
    val forename: String = "Unknown",
    val name: String = "Teacher",
    val shortName: String = "---"
)
