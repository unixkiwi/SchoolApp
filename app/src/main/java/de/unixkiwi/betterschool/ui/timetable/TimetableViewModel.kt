package de.unixkiwi.betterschool.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.unixkiwi.betterschool.core.models.SchoolWeek
import de.unixkiwi.betterschool.data.timetable.TimetableRepository
import de.unixkiwi.betterschool.data.timetable.TimetableWeekResult
import de.unixkiwi.betterschool.data.timetable.groupedForTimetable
import de.unixkiwi.betterschool.utils.WeekString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
) : ViewModel() {
    companion object {
        private const val TAG = "TimetableViewModel"
    }

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    fun updateSelectedPage(page: Int) {
        val currentState = _uiState.value
        if (currentState.isSuccess()) {
            _uiState.value = currentState.copy(index = page)
        }
    }

    fun goToCurrentDay() {
        updateWeek(
            WeekString.fromDateSmart(LocalDate.now()),
            isGoBackAction = false,
            forceRefresh = false
        )
    }

    fun updateCurrentWeek() {
        val currentState = _uiState.value
        if (currentState.isSuccess()) {
            updateWeek(
                currentState.weekString!!,
                isGoBackAction = false,
                forceRefresh = true,
                requestedIndex = currentState.index
            )
        } else {
            updateWeek(
                WeekString.fromDateSmart(LocalDate.now()),
                isGoBackAction = false,
                forceRefresh = true
            )
        }
    }

    fun updateWeek(
        weekString: WeekString,
        isGoBackAction: Boolean = false,
        forceRefresh: Boolean = false,
        requestedIndex: Int? = null
    ) {
        Timber.tag(TAG)
            .d("updateWeek called with week: $weekString, isGoBackAction: $isGoBackAction")

        val currentState = _uiState.value
        if (currentState.isSuccess() && !forceRefresh && currentState.weekString == weekString) {
            Timber.tag(TAG).d("Already showing the requested week! Just updating index.")

            viewModelScope.launch(Dispatchers.Default) {
                _uiState.update {
                    it.copy(
                        index = requestedIndex ?: getIndex(
                            weekString,
                            isGoBackAction,
                            currentState.week!!
                        )
                    )
                }
            }

            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            timetableRepository.getWeek(weekString.toString(), useLocal = forceRefresh)
                .collect { result ->
                    result.onSuccess { timetableWeekResult ->
                        when (timetableWeekResult) {
                            is TimetableWeekResult.Data -> {
                                val groupedWeek = timetableWeekResult.week.groupedForTimetable()

                                val index =
                                    requestedIndex ?: getIndex(
                                        weekString,
                                        isGoBackAction,
                                        groupedWeek
                                    )

                                _uiState.update {
                                    it.copy(
                                        week = groupedWeek,
                                        index = index,
                                        weekString = weekString,
                                        loading = timetableWeekResult.loading
                                    )
                                }
                            }

                            is TimetableWeekResult.Loading -> {
                                _uiState.update { it.copy(loading = it.loading) }
                            }
                        }
                    }.onFailure { throwable ->
                        Timber.tag(TAG).e(throwable, "updateWeek failed")
                        _uiState.update { it.copy(error = throwable) }
                    }
                }
        }
    }

    fun goToPreviousWeek() {
        val currentState = _uiState.value
        if (currentState.weekString != null) {
            val previousWeek = currentState.weekString.previousWeek()
            updateWeek(previousWeek, isGoBackAction = true)
        }
    }

    fun goToNextWeek() {
        val currentState = _uiState.value
        if (currentState.weekString != null) {
            val nextWeek = currentState.weekString.nextWeek()
            updateWeek(nextWeek, isGoBackAction = false)
        }
    }

    private fun getIndex(
        weekString: WeekString,
        isGoBackAction: Boolean = false,
        groupedWeek: SchoolWeek
    ): Int {
        val now = LocalDate.now()

        return if (weekString == WeekString.fromDate(now)) {
            min(now.dayOfWeek.value - 1, groupedWeek.days.size - 1)
        } else {
            if (isGoBackAction) {
                groupedWeek.days.size - 1
            } else {
                0
            }
        }
    }

    init {
        Timber.tag(TAG).d("init called")
        updateWeek(WeekString.fromDateSmart(LocalDate.now()))
    }
}

data class TimetableUiState(
    val week: SchoolWeek? = null,
    val index: Int? = null,
    val weekString: WeekString? = null,
    val loading: Boolean = true,
    val error: Throwable? = null
) {
    fun isSuccess(): Boolean {
        return week != null && weekString != null && index != null
    }

    fun isErrorFull(): Boolean {
        return week == null && weekString == null && error != null
    }

    fun isErrorWithData(): Boolean {
        return error != null
    }

    fun isLoadingFull(): Boolean {
        return loading && week == null && weekString == null
    }

    fun isLoadingWithData(): Boolean {
        return loading && week != null && weekString != null
    }
}