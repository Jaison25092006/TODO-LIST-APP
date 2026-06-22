package com.example.taskmasterpro.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.theme.TaskCompletedGreen
import com.example.taskmasterpro.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onPrioritySelect: (TaskPriority?) -> Unit,
    onSortSelect: (SortOption) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onUndoDelete: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onLogoutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    val categories = listOf("All", "Work", "Personal", "Study", "Health", "Shopping")
    var showSortMenu by remember { mutableStateOf(false) }
    var showPriorityMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onDeleteWrapper = { taskToDelete: Task ->
        onDeleteTask(taskToDelete)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Task \"${taskToDelete.title}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete()
            }
        }
    }

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent, // Let PremiumBackground show through
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "TaskMaster Pro",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = state.userEmail ?: "Guest Session",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onCalendarClick) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar")
                        }
                        IconButton(onClick = onAnalyticsClick) {
                            Icon(imageVector = Icons.Default.BarChart, contentDescription = "Analytics")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = onLogoutClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTaskClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
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
                // Stats Grid Section
                StatsGrid(
                    total = state.totalTasksCount,
                    completed = state.completedTasksCount,
                    pending = state.pendingTasksCount,
                    today = state.todayTasksCount
                )

                // Search and Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassyTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = "Search tasks...",
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.weight(1f)
                    )

                    // Sort Button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                            ),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Options")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Due Date") },
                                onClick = {
                                    onSortSelect(SortOption.DUE_DATE)
                                    showSortMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.DateRange, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Priority") },
                                onClick = {
                                    onSortSelect(SortOption.PRIORITY)
                                    showSortMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.PriorityHigh, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort by Title") },
                                onClick = {
                                    onSortSelect(SortOption.TITLE)
                                    showSortMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.SortByAlpha, null) }
                            )
                        }
                    }

                    // Priority Dropdown Filter Button
                    Box {
                        IconButton(
                            onClick = { showPriorityMenu = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (state.selectedPriority != null) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                }
                            ),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = "Priority Filter")
                        }
                        DropdownMenu(
                            expanded = showPriorityMenu,
                            onDismissRequest = { showPriorityMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Priorities") },
                                onClick = {
                                    onPrioritySelect(null)
                                    showPriorityMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("High Priority") },
                                onClick = {
                                    onPrioritySelect(TaskPriority.HIGH)
                                    showPriorityMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Medium Priority") },
                                onClick = {
                                    onPrioritySelect(TaskPriority.MEDIUM)
                                    showPriorityMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Low Priority") },
                                onClick = {
                                    onPrioritySelect(TaskPriority.LOW)
                                    showPriorityMenu = false
                                }
                            )
                        }
                    }
                }

                // Category Chips Section
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = if (category == "All") state.selectedCategory == null else state.selectedCategory == category
                        GlassyCategoryChip(
                            category = category,
                            selected = isSelected,
                            onClick = {
                                if (category == "All") onCategorySelect(null) else onCategorySelect(category)
                            }
                        )
                    }
                }

                // Tasks List Section
                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.tasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "All tasks completed! 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.tasks, key = { it.id }) { task ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        onDeleteWrapper(task)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val isDismissed = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                                    val color = if (isDismissed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) else Color.Transparent
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(color)
                                            .border(
                                                width = 1.dp,
                                                color = if (isDismissed) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else Color.Transparent,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (isDismissed) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteSweep,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                },
                                content = {
                                    TaskItem(
                                        task = task,
                                        onToggleCompletion = { onToggleCompletion(task) },
                                        onDelete = { onDeleteWrapper(task) },
                                        onClick = { onTaskClick(task.id) },
                                        modifier = Modifier.animateItem()
                                    )
                                },
                                enableDismissFromStartToEnd = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassyCategoryChip(
    category: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgSelected = MaterialTheme.colorScheme.primary
    val bgUnselected = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.05f) else Color(0xFF000000).copy(alpha = 0.04f)
    val borderUnselected = if (isDark) Color(0xFFFFFFFF).copy(alpha = 0.12f) else Color(0xFF000000).copy(alpha = 0.1f)
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) bgSelected else bgUnselected)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else borderUnselected,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatsGrid(total: Int, completed: Int, pending: Int, today: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Total Tasks",
                count = total,
                icon = Icons.AutoMirrored.Filled.ListAlt,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Due Today",
                count = today,
                icon = Icons.Default.Today,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsCard(
                title = "Pending",
                count = pending,
                icon = Icons.Default.PendingActions,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Completed",
                count = completed,
                icon = Icons.Default.CheckCircle,
                color = TaskCompletedGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassyCard(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    GlassyCard(
        shape = RoundedCornerShape(20.dp),
        surfaceAlpha = if (isCompleted) 0.4f else 0.7f,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleCompletion) {
                Icon(
                    imageVector = if (isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (isCompleted) TaskCompletedGreen else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isCompleted) 0.5f else 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = task.category)
                    TaskPriorityBadge(priority = task.priority)

                    Spacer(modifier = Modifier.weight(1f))

                    // Due Date Info
                    val formatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                    val dateString = formatter.format(Date(task.dueDate))
                    Text(
                        text = "$dateString at ${task.dueTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

