package de.unixkiwi.betterschool.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

val LocalDate.Companion.now: LocalDate
    get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
