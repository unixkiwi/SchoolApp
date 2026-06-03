package de.unixkiwi.betterschool.core.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.serialization.Serializable

@Serializable
data class SchoolDay(
    @Serializable(with = LocalDateIso8601Serializer::class)
    val date: LocalDate?,
    val lessons: List<SchoolLesson>,
    val notes: List<SchoolJournalNote>
)
