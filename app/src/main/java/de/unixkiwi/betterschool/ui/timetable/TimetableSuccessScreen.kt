package de.unixkiwi.betterschool.ui.timetable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import de.unixkiwi.betterschool.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimetableSuccessScreen(
    uiState: TimetableUiState,
    onPageChanged: (Int) -> Unit,
    onPrevWeekBtnClick: () -> Unit,
    onNextWeekBtnClick: () -> Unit,
    onCurrentDayBtnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    key(uiState.weekString!!) {
        val pagerState =
            rememberPagerState(
                pageCount = { uiState.week!!.days.size },
                initialPage = uiState.index!!
            )

        LaunchedEffect(uiState.weekString, uiState.index) {
            if (pagerState.currentPage != uiState.index) {
                pagerState.scrollToPage(uiState.index)
            }
        }

        LaunchedEffect(pagerState.currentPage) {
            onPageChanged(pagerState.currentPage)
        }

        Box {
            HorizontalFloatingToolbar(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -ScreenOffset)
                        .zIndex(1f),
                colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onCurrentDayBtnClick,
                        content = {
                            Icon(
                                painterResource(R.drawable.calendar_month), null
                            )
                        })
                },
                expanded = true
            ) {
                listOf(
                    R.drawable.arrow_back_24px,
                    R.drawable.arrow_forward_24px
                ).forEachIndexed { index, icon ->
                    IconButton(onClick = {
                        if (index == 0) {
                            onPrevWeekBtnClick()
                        } else {
                            onNextWeekBtnClick()
                        }
                    }) {
                        Icon(
                            painterResource(icon),
                            null
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = modifier
            ) { page ->
                if (uiState.week!!.days[page].lessons.isEmpty()) {
                    TimetableEmptyDayScreen()
                } else
                    TimetableSuccessList(
                        uiState = uiState,
                        page = page
                    )
            }
        }
    }
}