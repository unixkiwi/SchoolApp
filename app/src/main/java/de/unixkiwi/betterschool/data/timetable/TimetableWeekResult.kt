package de.unixkiwi.betterschool.data.timetable

import de.unixkiwi.betterschool.core.models.SchoolWeek

sealed class TimetableWeekResult {
    class Loading : TimetableWeekResult()
    data class Data(val week: SchoolWeek, val loading: Boolean) : TimetableWeekResult()
}