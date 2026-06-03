package de.unixkiwi.betterschool.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.unixkiwi.betterschool.core.models.SchoolWeek
import de.unixkiwi.betterschool.data.auth.AuthRepository
import de.unixkiwi.betterschool.data.timetable.TimetableRepository
import de.unixkiwi.betterschool.data.timetable.groupedForTimetable
import de.unixkiwi.betterschool.utils.WeekString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val timetableRepository: TimetableRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    companion object {
        private const val TAG = "TimetableViewModel"
    }

    private val _uiState = MutableStateFlow<TimetableUiState>(TimetableUiState.Loading)
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    fun updateSelectedPage(page: Int) {
        val currentState = _uiState.value
        if (currentState is TimetableUiState.Success) {
            _uiState.value = currentState.copy(index = page)
        }
    }

    fun updateWeek(week: WeekString, isGoBackAction: Boolean = false) {
        Timber.tag(TAG).d("updateWeek called with week: $week, isGoBackAction: $isGoBackAction")
        viewModelScope.launch(Dispatchers.Default) {
            timetableRepository.getWeek(week.toString()).collect { result ->
                result.onSuccess { week ->
                    val groupedWeek = week.groupedForTimetable()

                    val now = LocalDate.now()

                    val index = if (week == WeekString.fromDateSmart(now)) {
                        if (now.dayOfWeek.value >= 6) {
                            0
                        } else {
                            now.dayOfWeek.value - 1
                        }
                    } else {
                        if (isGoBackAction) {
                            groupedWeek.days.size - 1
                        } else {
                            0
                        }
                    }

                    _uiState.update { TimetableUiState.Success(groupedWeek, index) }
                }.onFailure { throwable ->
                    Timber.tag(TAG).e(throwable, "updateWeek failed")
                    _uiState.update { TimetableUiState.Error(throwable) }
                }
            }
        }
    }

    fun goToPreviousWeek() {
        val currentState = _uiState.value
        if (currentState is TimetableUiState.Success) {
            val currentWeek = currentState.week
            val firstDay = currentWeek.days.firstOrNull()?.date
            val previousWeek = if (firstDay == null) {
                Timber.tag(TAG).w("Current week has no days, settings to week before current")
                WeekString.fromDateSmart(LocalDate.now()).previousWeek()
            } else {
                WeekString.fromDate(firstDay.toJavaLocalDate()).previousWeek()
            }
            updateWeek(previousWeek, isGoBackAction = true)
        }
    }

    fun goToNextWeek() {
        val currentState = _uiState.value
        if (currentState is TimetableUiState.Success) {
            val currentWeek = currentState.week
            val firstDay = currentWeek.days.firstOrNull()?.date
            val previousWeek = if (firstDay == null) {
                Timber.tag(TAG).w("Current week has no days, settings to week before current")
                WeekString.fromDateSmart(LocalDate.now()).nextWeek()
            } else {
                WeekString.fromDate(firstDay.toJavaLocalDate()).nextWeek()
            }
            updateWeek(previousWeek, isGoBackAction = false)
        }
    }

    init {
        Timber.tag(TAG).d("init called")
        updateWeek(WeekString.fromDateSmart(LocalDate.now()))
    }
}

sealed interface TimetableUiState {
    data object Loading : TimetableUiState
    data class Success(val week: SchoolWeek, val index: Int /*TODO: , val isLoading: Boolean*/) :
        TimetableUiState

    data class Error(val error: Throwable) : TimetableUiState
}