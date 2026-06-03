package de.unixkiwi.betterschool.core.models

import kotlinx.serialization.Serializable

@Serializable
data class SchoolLesson(
    val nr: String,
    val status: SchoolLessonStatus,
    val source: String?,
    val subject: SchoolSubject,
    val teachers: List<SchoolTeacher>,
    val group: SchoolGroup,
    val rooms: List<SchoolRoom>,
    val notes: List<SchoolJournalNote>,
    val subLessons: List<SchoolLesson> = emptyList()
)
