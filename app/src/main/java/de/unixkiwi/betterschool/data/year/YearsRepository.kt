package de.unixkiwi.betterschool.data.year

import de.unixkiwi.betterschool.core.local.ApiCacheFileManager
import de.unixkiwi.betterschool.data.auth.AuthRepository
import de.unixkiwi.betterschool.utils.now
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import timber.log.Timber

class YearsRepository(
    private val remote: RemoteYearSource,
    private val authRepo: AuthRepository,
    private val cache: ApiCacheFileManager
) {
    private val cacheKey = "years"

    suspend fun getCurrentYear(forceRefresh: Boolean = false): Result<BesteSchuleYear> {
        return getYearForDate(LocalDate.now, forceRefresh)
    }

    suspend fun getYearForDate(
        date: LocalDate,
        forceRefresh: Boolean = false
    ): Result<BesteSchuleYear> {
        val years = getYears(forceRefresh)
        val yearsList = years.getOrNull()
            ?: return Result.failure(Throwable("Failure while calling getYears(): ${years.exceptionOrNull()?.message}"))

        for (year in yearsList) {
            if (date in year.from()..year.to()) return Result.success(year)
        }

        return Result.failure(Throwable("No year found with that date!"))
    }

    suspend fun getYears(forceRefresh: Boolean = false): Result<List<BesteSchuleYear>> {
        val shouldUseRemote =
            forceRefresh //TODO cache older than 3 days

        if (shouldUseRemote) {
            try {
                val authHeader = authRepo.getAuthHeader()
                return if (authHeader.isNullOrEmpty()) {
                    Result.failure(Throwable("Token was null!"))
                } else {
                    val years = remote.getYears(authHeader).data
                    if (years.isNotEmpty()) cache.store(cacheKey, Json.encodeToString(years))

                    Result.success(years)
                }
            } catch (e: Exception) {
                Timber.e("Error while getting year from remote: $e")
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