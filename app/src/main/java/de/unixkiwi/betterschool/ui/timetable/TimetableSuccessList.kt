package de.unixkiwi.betterschool.ui.timetable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun TimetableSuccessList(
    uiState: TimetableUiState,
    page: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(6.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                "Lessons",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            )
        }
        itemsIndexed(uiState.week!!.days[page].lessons) { index, lesson ->
            TimetableListItem(
                lesson = lesson,
                index = index,
                listSize = uiState.week.days[page].lessons.size
            )
            Spacer(modifier = Modifier.height(ListItemDefaults.SegmentedGap))
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            Text(
                "Notes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(uiState.week.days[page].notes) { index, note ->
            ListItem(
                onClick = { },
                shapes = if (uiState.week.days[page].notes.size == 1) {
                    ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
                } else {
                    ListItemDefaults.segmentedShapes(
                        index = index,
                        count = uiState.week.days[page].notes.size
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                content = {
                    Text(
                        note.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}