package de.unixkiwi.betterschool.data.year

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class BesteSchuleYear(
    val id: Int,
    val name: String,
    val from: String,
    val to: String
) {
    fun from(): LocalDate {
        return LocalDate.parse(from)
    }

    fun to(): LocalDate {
        return LocalDate.parse(to)
    }
}