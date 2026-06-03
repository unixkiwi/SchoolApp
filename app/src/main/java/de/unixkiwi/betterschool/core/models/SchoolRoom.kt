package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolRoom(
    val name: String = "---"
)
