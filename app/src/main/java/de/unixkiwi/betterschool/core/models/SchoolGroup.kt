package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolGroup(
    val name: String = "Unknown Group",
    val shortName: String = "UNK",
    val meta: Boolean = false,
)
