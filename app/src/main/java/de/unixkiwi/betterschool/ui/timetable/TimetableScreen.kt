package de.unixkiwi.betterschool.ui.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.unixkiwi.betterschool.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char


@Composable
fun TimetableScreen(viewModel: TimetableViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimetableScreen(
        uiState = uiState,
        onPageChanged = { viewModel.updateSelectedPage(it) },
        onPrevWeekBtnClick = { viewModel.goToPreviousWeek() },
        onNextWeekBtnClick = { viewModel.goToNextWeek() },
        onCurrentDayBtnClick = { viewModel.goToCurrentDay() },
        onUpdateBtnClick = { viewModel.updateCurrentWeek() }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TimetableScreen(
    uiState: TimetableUiState,
    onPageChanged: (Int) -> Unit,
    onPrevWeekBtnClick: () -> Unit,
    onNextWeekBtnClick: () -> Unit,
    onCurrentDayBtnClick: () -> Unit,
    onUpdateBtnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (uiState is TimetableUiState.Success) {
                        val date = uiState.week.days[uiState.index].date?.format(
                            LocalDate.Format {
                                day()
                                char('.')
                                char(' ')
                                monthName(MonthNames.ENGLISH_ABBREVIATED)
                                char(' ')
                                year()
                            }
                        ) ?: "Timetable?"
                        Text(date)
                    } else
                        Text("Timetable")
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(R.drawable.settings_filled),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUpdateBtnClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh_rounded_24dp),
                            contentDescription = null
                        )
                    }
                }
            )
        },

        ) { innerPad ->
        when (uiState) {
            is TimetableUiState.Loading -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPad)
            ) {
                ContainedLoadingIndicator(modifier = Modifier.size(120.dp))
            }

            is TimetableUiState.Error -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(innerPad)
            ) { Text("Error: ${uiState.error}") }

            is TimetableUiState.Success -> {
                TimetableSuccessComponent(
                    uiState,
                    onPageChanged,
                    onPrevWeekBtnClick,
                    onNextWeekBtnClick,
                    onCurrentDayBtnClick,
                    Modifier
                        .fillMaxSize()
                        .padding(innerPad)
                )
            }
        }
    }
}