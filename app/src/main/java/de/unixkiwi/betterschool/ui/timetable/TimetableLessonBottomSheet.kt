package de.unixkiwi.betterschool.ui.timetable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.unixkiwi.betterschool.core.models.SchoolLesson

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimetableListItemBottomSheet(
    lesson: SchoolLesson,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                lesson.subject.name,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))

            TimetableListItemBottomSheetSection(
                title = if (lesson.teachers.size > 1) "Teachers" else "Teacher",
                items = lesson.teachers,
            ) { teacher, index, count ->
                ListItem(
                    shapes = if (count == 1) {
                        ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
                    } else {
                        ListItemDefaults.segmentedShapes(
                            index = index,
                            count = count
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    onClick = {},
                    content = {
                        Text(
                            teacher.forename + " " + teacher.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    supportingContent = {
                        Text(
                            teacher.shortName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }

            if (lesson.rooms.isNotEmpty() || lesson.subLessons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            TimetableListItemBottomSheetSection(
                title = if (lesson.rooms.size > 1) "Rooms" else "Room",
                items = lesson.rooms,
            ) { room, index, count ->
                ListItem(
                    shapes = if (count == 1) {
                        ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
                    } else {
                        ListItemDefaults.segmentedShapes(
                            index = index,
                            count = count
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    onClick = {},
                    content = {
                        Text(room.name, style = MaterialTheme.typography.bodyLarge)
                    }
                )
            }

            if (lesson.rooms.isNotEmpty() && lesson.subLessons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            TimetableListItemBottomSheetSection(
                title = if (lesson.subLessons.size > 1) "Sub-lessons" else "Sub-lesson",
                items = lesson.subLessons,
            ) { subLesson, index, count ->
                TimetableListItem(
                    lesson = subLesson,
                    index = index,
                    listSize = count,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            if (lesson.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            TimetableListItemBottomSheetSection(
                title = "Notes",
                items = lesson.notes,
            ) { note, index, count ->
                ListItem(
                    shapes = if (count == 1) {
                        ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
                    } else {
                        ListItemDefaults.segmentedShapes(
                            index = index,
                            count = count
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    onClick = {},
                    content = {
                        Text(note.description, style = MaterialTheme.typography.bodyLarge)
                    },
                    overlineContent = {
                        Text(note.type, style = MaterialTheme.typography.bodyMedium)
                    }
                )
            }
        }
    }
}

@Composable
private fun <T> TimetableListItemBottomSheetSection(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    itemSpacing: androidx.compose.ui.unit.Dp = 2.dp,
    itemContent: @Composable (item: T, index: Int, count: Int) -> Unit,
) {
    if (items.isEmpty()) {
        return
    }

    Column(modifier = modifier) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(2.dp))

        items.forEachIndexed { index, item ->
            itemContent(item, index, items.size)

            if (index < items.lastIndex) {
                Spacer(modifier = Modifier.height(itemSpacing))
            }
        }
    }
}

