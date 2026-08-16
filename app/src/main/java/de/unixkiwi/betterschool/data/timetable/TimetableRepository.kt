package de.unixkiwi.betterschool.data.timetable

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import de.unixkiwi.betterschool.core.local.ApiCacheFileManager
import de.unixkiwi.betterschool.core.models.SchoolDay
import de.unixkiwi.betterschool.core.models.SchoolGroup
import de.unixkiwi.betterschool.core.models.SchoolJournalNote
import de.unixkiwi.betterschool.core.models.SchoolLesson
import de.unixkiwi.betterschool.core.models.SchoolLessonStatus
import de.unixkiwi.betterschool.core.models.SchoolRoom
import de.unixkiwi.betterschool.core.models.SchoolSubject
import de.unixkiwi.betterschool.core.models.SchoolTeacher
import de.unixkiwi.betterschool.core.models.SchoolWeek
import de.unixkiwi.betterschool.data.auth.AuthRepository
import de.unixkiwi.betterschool.data.year.YearsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import timber.log.Timber

class TimetableRepository(
    private val remoteSource: RemoteTimetableSource,
    private val yearsRepo: YearsRepository,
    private val cache: ApiCacheFileManager,
    private val context: Context,
    private val authRepo: AuthRepository
) {
    /**
     * @param filterYear ID of the year, null means use selected
     * @param useLocal flow: emit(local) -> if (onMobileData) exit else emit(remote)
     */
    fun getWeek(
        weekId: String,
        filterYear: Int? = null,
        useLocal: Boolean = true
    ): Flow<Result<TimetableWeekResult>> = flow {
        emit(Result.success(TimetableWeekResult.Loading()))

        val authHeader = authRepo.getAuthHeader()
        if (authHeader.isNullOrEmpty()) {
            emit(Result.failure(Throwable("Token was null!")))
            return@flow
        }

        val yearId: Int? = filterYear ?: yearsRepo.getCurrentYear().getOrNull()?.id

        val cacheKey = "${weekId}__${yearId}"

        if (useLocal) {
            try {
                Timber.v("Getting timetable from local!")
                val week = cache.get(cacheKey)?.let { Json.decodeFromString<SchoolWeek>(it) }
                if (week != null) {
                    val mobileDataActive = isMobileDataActive()
                    Timber.i("Got timetable stuff from cache (${cacheKey})")
                    emit(
                        Result.success(
                            TimetableWeekResult.Data(
                                week,
                                !mobileDataActive /*TODO mobile data setting */
                            )
                        )
                    )

                    if (mobileDataActive) return@flow
                } else {
                    Timber.e("Timetable local null")
                    if (isMobileDataActive()) {
                        emit(Result.failure(Throwable("Cache is null!")))
                        emit(Result.success(TimetableWeekResult.Loading(false)))
                        return@flow
                    }
                }
            } catch (e: Exception) {
                Timber.e("Local cache request failed: $e")
                if (isMobileDataActive()) {
                    emit(Result.failure(e))
                    emit(Result.success(TimetableWeekResult.Loading(false)))
                    return@flow
                }
            }
        }

        try {
            Timber.d("Getting timetable from remote")
            val rawData =
                remoteSource.getWeek(weekId, filterYear = yearId, authHeader = authHeader).data

            val schoolWeek = rawData.toSchoolWeek()

            Timber.i("Got Data from remote")

            if (yearId != null) {
                cache.store(cacheKey, Json.encodeToString(schoolWeek))
                Timber.i("Cached timetabled data for $cacheKey")
            } else {
                Timber.e("YearID null, not caching timetable!")
            }

            emit(Result.success(TimetableWeekResult.Data(schoolWeek, false)))
            return@flow
        } catch (e: Exception) {
            Timber.e(e)
            emit(Result.failure(e))
        }

        emit(Result.success(TimetableWeekResult.Loading(false)))
        return@flow
    }

    private fun BesteSchuleJournalWeek.toSchoolWeek(): SchoolWeek {
        return SchoolWeek(
            days = days.map { day ->
                SchoolDay(
                    date = day.date?.let { LocalDate.parse(it) },
                    lessons = day.lessons.map { lesson ->
                        SchoolLesson(
                            nr = lesson.nr,
                            status = if (lesson.status == null) SchoolLessonStatus.INITIAL else SchoolLessonStatus.fromApiValue(
                                lesson.status
                            ),
                            source = lesson.source,
                            subject = SchoolSubject(
                                shortName = lesson.subject.local_id ?: SchoolSubject().shortName,
                                name = lesson.subject.name ?: SchoolSubject().name
                            ),
                            teachers = lesson.teachers.map { teacher ->
                                SchoolTeacher(
                                    forename = teacher.forename ?: SchoolTeacher().forename,
                                    name = teacher.name ?: SchoolTeacher().name,
                                    shortName = teacher.local_id ?: SchoolTeacher().shortName
                                )
                            },
                            group = SchoolGroup(
                                name = lesson.group.name ?: SchoolGroup().name,
                                shortName = lesson.group.local_id ?: SchoolGroup().shortName,
                                meta = lesson.group.meta > 0
                            ),
                            rooms = lesson.rooms.map { room ->
                                SchoolRoom(
                                    name = room.local_id
                                )
                            },
                            notes = lesson.notes.map { note ->
                                SchoolJournalNote(
                                    description = note.description
                                        ?: SchoolJournalNote().description,
                                    type = note.type.name ?: SchoolJournalNote().type
                                )
                            }
                        )
                    },
                    notes = processNotes(day.notes)
                )
            }
        )
    }

    private fun processNotes(notes: List<BesteSchuleJournalNote>): List<SchoolJournalNote> {
        val tempNotes = mutableListOf<SchoolJournalNote>()

        val notesBundled = mutableListOf<SchoolJournalNote>()

        for (note in notes) {
            if (note.description.isNullOrEmpty()) {
                if (tempNotes.isNotEmpty()) {
                    notesBundled += SchoolJournalNote(description = tempNotes.joinToString(separator = "\n") { it.description })
                    tempNotes.clear()
                }
            } else {
                tempNotes += SchoolJournalNote(description = note.description)
            }
        }

        if (tempNotes.isNotEmpty()) {
            notesBundled += SchoolJournalNote(description = tempNotes.joinToString(separator = "\n") { it.description })
            tempNotes.clear()
        }

        //TODO settings for bundled vs separate settings
        val notesSeparated = notes.filter { !it.description.isNullOrEmpty() }
            .map {
                SchoolJournalNote(
                    description = it.description ?: "No Content",
                    type = it.type.name ?: "No Type"
                )
            }

        return notesBundled
    }

    private fun isOffline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return true

        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isMobileDataActive(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}