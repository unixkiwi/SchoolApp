package de.unixkiwi.betterschool.ui.timetable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
fun TimetableScreen(
    onMenuBtnClicked: () -> Unit,
    onLoginBtnClick: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimetableScreen(
        uiState = uiState,
        onMenuBtnClicked = onMenuBtnClicked,
        onPageChanged = { viewModel.updateSelectedPage(it) },
        onPrevWeekBtnClick = { viewModel.goToPreviousWeek() },
        onNextWeekBtnClick = { viewModel.goToNextWeek() },
        onCurrentDayBtnClick = { viewModel.goToCurrentDay() },
        onUpdateBtnClick = { viewModel.updateCurrentWeek() },
        onLoginBtnClick = {
            viewModel.clearToken()
            onLoginBtnClick()
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TimetableScreen(
    uiState: TimetableUiState,
    onMenuBtnClicked: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onPrevWeekBtnClick: () -> Unit,
    onNextWeekBtnClick: () -> Unit,
    onCurrentDayBtnClick: () -> Unit,
    onUpdateBtnClick: () -> Unit,
    onLoginBtnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteRotation = rememberInfiniteTransition(label = "infiniteRotation")
    val rotation by infiniteRotation.animateFloat(
        0F,
        360F,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (uiState.isSuccess()) {
                        val date = uiState.week!!.days[uiState.index!!].date?.format(
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
                    IconButton(
                        onClick = onMenuBtnClicked,
                        shape = MaterialShapes.Cookie4Sided.toShape(),
                        colors = IconButtonDefaults.iconButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.menu_24px),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onUpdateBtnClick,
                        shape = MaterialShapes.Cookie9Sided.toShape(),
                        colors = IconButtonDefaults.iconButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.graphicsLayer {
                            if (uiState.isLoadingWithData()) rotationZ = rotation
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh_rounded_24dp),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { innerPad ->
        if (uiState.isLoadingFull()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPad)
            ) {
                ContainedLoadingIndicator(modifier = Modifier.size(120.dp))
            }
        } else if (uiState.isErrorFull()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPad)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cancel_rounded_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Something went wrong",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    uiState.error?.localizedMessage ?: "Unknown error",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onUpdateBtnClick,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Text("Retry")
                }
                if (uiState.isAuthError()) {
                    Button(
                        onClick = onLoginBtnClick,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Login")
                    }
                }
            }
        } else if (uiState.isSuccess()) {
            TimetableSuccessScreen(
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