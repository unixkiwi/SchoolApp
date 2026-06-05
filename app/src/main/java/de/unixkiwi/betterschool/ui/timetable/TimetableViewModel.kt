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
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.min

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

    fun goToCurrentDay() {
        updateWeek(
            WeekString.fromDateSmart(LocalDate.now()),
            isGoBackAction = false,
            forceRefresh = false
        )
    }

    fun updateCurrentWeek() {
        val currentState = _uiState.value
        if (currentState is TimetableUiState.Success) {
            updateWeek(
                currentState.weekString,
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
        if (currentState is TimetableUiState.Success && !forceRefresh && currentState.weekString == weekString) {
            Timber.tag(TAG).d("Already showing the requested week! Just updating index.")

            viewModelScope.launch(Dispatchers.Default) {
                _uiState.value = currentState.copy(
                    index = requestedIndex ?: getIndex(
                        weekString,
                        isGoBackAction,
                        currentState.week
                    )
                )
            }

            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            timetableRepository.getWeek(weekString.toString()).collect { result ->
                result.onSuccess { week ->
                    val groupedWeek = week.groupedForTimetable()

                    val index = requestedIndex ?: getIndex(weekString, isGoBackAction, groupedWeek)

                    _uiState.update { TimetableUiState.Success(groupedWeek, index, weekString) }
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
            val previousWeek = currentState.weekString.previousWeek()
            updateWeek(previousWeek, isGoBackAction = true)
        }
    }

    fun goToNextWeek() {
        val currentState = _uiState.value
        if (currentState is TimetableUiState.Success) {
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

sealed interface TimetableUiState {
    data object Loading : TimetableUiState
    data class Success(
        val week: SchoolWeek,
        val index: Int,
        val weekString: WeekString /*TODO: , val isLoading: Boolean*/
    ) :
        TimetableUiState

    data class Error(val error: Throwable) : TimetableUiState
}