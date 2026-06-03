package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolWeek(
    val days: List<SchoolDay>
)
