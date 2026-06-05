package de.unixkiwi.betterschool.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.unixkiwi.betterschool.R
import de.unixkiwi.betterschool.core.models.SchoolLesson
import de.unixkiwi.betterschool.core.models.SchoolLessonStatus


private fun getIconForStatus(status: SchoolLessonStatus): Int {
    return when (status) {
        SchoolLessonStatus.CANCELED -> R.drawable.ic_cancel_rounded_24dp
        SchoolLessonStatus.PLANNED -> R.drawable.ic_calendar_clock_rounded_24dp
        SchoolLessonStatus.HOLD -> R.drawable.ic_task_alt_rounded_24dp
        SchoolLessonStatus.INITIAL -> R.drawable.ic_pending_rounded_24dp
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun getShapeForStatus(status: SchoolLessonStatus): Shape {
    return when (status) {
        SchoolLessonStatus.CANCELED -> MaterialShapes.PixelCircle.toShape()
        SchoolLessonStatus.PLANNED -> MaterialShapes.Cookie4Sided.toShape()
        SchoolLessonStatus.HOLD -> MaterialShapes.Cookie12Sided.toShape()
        SchoolLessonStatus.INITIAL -> MaterialShapes.Sunny.toShape()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimetableListItem(
    lesson: SchoolLesson,
    index: Int,
    listSize: Int,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    if (showBottomSheet) {
        TimetableListItemBottomSheet(
            lesson = lesson,
            onDismissRequest = { showBottomSheet = false }
        )
    }

    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = when (lesson.status) {
                SchoolLessonStatus.CANCELED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75F)
                SchoolLessonStatus.PLANNED -> MaterialTheme.colorScheme.surfaceContainerHighest
                SchoolLessonStatus.HOLD -> MaterialTheme.colorScheme.surfaceContainerLow
                SchoolLessonStatus.INITIAL -> MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        onClick = {
            showBottomSheet = true
        },
        shapes = if (listSize == 1) {
            ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
        } else {
            ListItemDefaults.segmentedShapes(
                index = index,
                count = listSize
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (lesson.status == SchoolLessonStatus.CANCELED) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary,
                        shape = getShapeForStatus(lesson.status)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(getIconForStatus(lesson.status)),
                    null,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(2.dp),
                    tint = if (lesson.status == SchoolLessonStatus.CANCELED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        content = {
            Text(
                if (lesson.subject.name.length > 20) lesson.subject.shortName else lesson.subject.name,
                style = if (lesson.status == SchoolLessonStatus.CANCELED) MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textDecoration = TextDecoration.LineThrough
                ) else MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                val cancelledSubLessons: List<SchoolLesson> =
                    lesson.subLessons.filter { it.status == SchoolLessonStatus.CANCELED }
                if (cancelledSubLessons.isNotEmpty()) {
                    Text(
                        cancelledSubLessons.joinToString(", ") { it.subject.shortName },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                val teacherLongText = lesson.teachers.joinToString(", ") { it.name }

                LazyRow {
                    if (lesson.teachers.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    painterResource(R.drawable.person),
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    if (teacherLongText.length < 21) teacherLongText else lesson.teachers.joinToString(
                                        ", "
                                    ) { it.shortName },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                )
                            }
                        }
                    }
                    if (lesson.rooms.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    painterResource(R.drawable.room),
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    lesson.rooms.joinToString(", ") { it.name },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                )
                            }
                        }
                        if (lesson.subLessons.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_list_alt_add_rounded_24dp),
                                        null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "More lessons",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}