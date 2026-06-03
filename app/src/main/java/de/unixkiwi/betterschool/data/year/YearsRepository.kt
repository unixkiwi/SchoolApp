package de.unixkiwi.betterschool.data.year

import de.unixkiwi.betterschool.core.local.ApiCacheFileManager
import de.unixkiwi.betterschool.data.auth.AuthRepository
import de.unixkiwi.betterschool.utils.now
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json

class YearsRepository(
    private val remote: RemoteYearSource,
    private val authRepo: AuthRepository,
    private val cache: ApiCacheFileManager
) {
    private var lastCache = LocalDate.now
    private val cacheKey = "years"

    suspend fun getCurrentYear(): Result<BesteSchuleYear> {
        return getYearForDate(LocalDate.now)
    }

    suspend fun getYearForDate(date: LocalDate): Result<BesteSchuleYear> {
        val years = getYears()
        val yearsList = years.getOrNull()
            ?: return Result.failure(Throwable("Failure while calling getYears(): ${years.exceptionOrNull()?.message}"))

        for (year in yearsList) {
            if (date in year.from()..year.to()) return Result.success(year)
        }

        return Result.failure(Throwable("No year found with that date!"))
    }

    suspend fun getYears(forceRefresh: Boolean = false): Result<List<BesteSchuleYear>> {
        val shouldUseRemote = !forceRefresh && LocalDate.now < lastCache.plus(3, DateTimeUnit.DAY)

        if (shouldUseRemote) {
            try {
                val token = authRepo.getToken()
                return if (token.isNullOrEmpty()) {
                    Result.failure(Throwable("Token was null!"))
                } else {
                    lastCache = LocalDate.now
                    val years = remote.getYears(token).data
                    if (years.isNotEmpty()) cache.store(cacheKey, Json.encodeToString(years))

                    Result.success(years)
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
        } else {
            return try {
                val years =
                    cache.get(cacheKey)?.let { Json.decodeFromString<List<BesteSchuleYear>>(it) }
                if (years.isNullOrEmpty()) Result.failure(Throwable("Cached data was null or empty!"))
                else Result.success(years)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}