package com.example.taskmasterpro.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.ui.components.GlassyCard
import com.example.taskmasterpro.ui.components.PremiumBackground
import com.example.taskmasterpro.ui.screens.dashboard.TaskItem
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDateCell(
    val day: Int,
    val isCurrentMonth: Boolean,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onDateSelected: (Long) -> Unit,
    onMonthChange: (Int) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onAddTaskClick: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val selectedDateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }

    // Calendar day grid calculations
    val cells = remember(state.currentMonth) {
        val grid = mutableListOf<CalendarDateCell>()
        val calendar = state.currentMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month padding
        val prevMonth = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val prevDaysInMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in firstDayOfWeek - 1 downTo 0) {
            val day = prevDaysInMonth - i
            val cal = (prevMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            grid.add(CalendarDateCell(day, isCurrentMonth = false, timestamp = getStartOfDay(cal)))
        }

        // Current month days
        for (day in 1..daysInMonth) {
            val cal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            grid.add(CalendarDateCell(day, isCurrentMonth = true, timestamp = getStartOfDay(cal)))
        }

        // Next month padding
        val remainingCells = if (grid.size <= 35) 35 - grid.size else 42 - grid.size
        val nextMonth = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        for (day in 1..remainingCells) {
            val cal = (nextMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
            grid.add(CalendarDateCell(day, isCurrentMonth = false, timestamp = getStartOfDay(cal)))
        }

        grid
    }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Calendar View", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onAddTaskClick(state.selectedDateMillis) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Calendar Container Card
                GlassyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Month Navigation Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { onMonthChange(-1) }) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                            }
                            Text(
                                text = monthYearFormat.format(state.currentMonth.time),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onMonthChange(1) }) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weekdays Header Row
                        val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weekdays.forEach { weekday ->
                                Text(
                                    text = weekday,
                                    modifier = Modifier.width(44.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid Column
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rows = cells.chunked(7)
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    row.forEach { cell ->
                                        val isSelected = isSameDay(cell.timestamp, state.selectedDateMillis)
                                        val dayTasks = state.taskMarkers[cell.timestamp] ?: emptyList()
                                        val pendingCount = dayTasks.count { it.status == com.example.taskmasterpro.data.model.TaskStatus.PENDING }

                                        CalendarDayCell(
                                            day = cell.day,
                                            isCurrentMonth = cell.isCurrentMonth,
                                            isSelected = isSelected,
                                            hasPendingTasks = pendingCount > 0,
                                            onClick = { onDateSelected(cell.timestamp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Date Tasks Heading
                Text(
                    text = selectedDateFormat.format(Date(state.selectedDateMillis)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )

                // Date Tasks List
                if (state.selectedDateTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks scheduled for this day",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.selectedDateTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                onToggleCompletion = { onToggleCompletion(task) },
                                onDelete = { onDeleteTask(task) },
                                onClick = { onTaskClick(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    hasPendingTasks: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                }
            )
            if (hasPendingTasks) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.error
                        )
                )
            }
        }
    }
}

private fun getStartOfDay(calendar: Calendar): Long {
    return calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

