package com.example.taskmasterpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.ui.screens.addtask.AddTaskScreen
import com.example.taskmasterpro.ui.screens.addtask.AddTaskViewModel
import com.example.taskmasterpro.ui.screens.analytics.AnalyticsScreen
import com.example.taskmasterpro.ui.screens.analytics.AnalyticsViewModel
import com.example.taskmasterpro.ui.screens.calendar.CalendarScreen
import com.example.taskmasterpro.ui.screens.calendar.CalendarViewModel
import com.example.taskmasterpro.ui.screens.dashboard.DashboardScreen
import com.example.taskmasterpro.ui.screens.dashboard.DashboardViewModel
import com.example.taskmasterpro.ui.screens.edittask.EditTaskScreen
import com.example.taskmasterpro.ui.screens.edittask.EditTaskViewModel
import com.example.taskmasterpro.ui.screens.auth.LoginScreen
import com.example.taskmasterpro.ui.screens.auth.RegisterScreen
import com.example.taskmasterpro.ui.screens.auth.AuthViewModel
import com.example.taskmasterpro.ui.screens.settings.SettingsScreen
import com.example.taskmasterpro.ui.screens.settings.SettingsViewModel
import com.example.taskmasterpro.ui.screens.splash.SplashScreen
import com.example.taskmasterpro.ui.screens.taskdetails.TaskDetailsScreen
import com.example.taskmasterpro.ui.screens.taskdetails.TaskDetailsViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    authRepository: AuthRepository
) {
    val backStack = rememberNavBackStack(SplashRoute)
    val scope = rememberCoroutineScope()

    fun navigateTo(route: Any, clearBackStack: Boolean = false) {
        if (clearBackStack) {
            while (backStack.size > 0) {
                backStack.removeLastOrNull()
            }
            backStack.add(route as NavKey)
        } else {
            backStack.add(route as NavKey)
        }
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { popBack() },
        entryProvider = entryProvider {
            entry<SplashRoute> {
                SplashScreen(
                    authRepository = authRepository,
                    onNavigateToDashboard = { navigateTo(DashboardRoute, clearBackStack = true) },
                    onNavigateToLogin = { navigateTo(LoginRoute, clearBackStack = true) }
                )
            }
            entry<LoginRoute> {
                val viewModel: AuthViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                
                if (state.isSuccess) {
                    LaunchedEffect(Unit) {
                        viewModel.resetSuccessState()
                        navigateTo(DashboardRoute, clearBackStack = true)
                    }
                }

                LoginScreen(
                    state = state,
                    onEmailChange = { viewModel.onEmailChange(it) },
                    onPasswordChange = { viewModel.onPasswordChange(it) },
                    onRememberMeToggle = { viewModel.onRememberMeToggle(it) },
                    onLoginClick = { viewModel.login() },
                    onForgotPasswordClick = { viewModel.sendPasswordReset() },
                    onRegisterClick = { navigateTo(RegisterRoute) }
                )
            }
            entry<RegisterRoute> {
                val viewModel: AuthViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                if (state.isSuccess) {
                    LaunchedEffect(Unit) {
                        viewModel.resetSuccessState()
                        navigateTo(DashboardRoute, clearBackStack = true)
                    }
                }

                RegisterScreen(
                    state = state,
                    onNameChange = { viewModel.onNameChange(it) },
                    onEmailChange = { viewModel.onEmailChange(it) },
                    onPasswordChange = { viewModel.onPasswordChange(it) },
                    onConfirmPasswordChange = { viewModel.onConfirmPasswordChange(it) },
                    onRegisterClick = { viewModel.register() },
                    onLoginClick = { popBack() }
                )
            }
            entry<DashboardRoute> {
                val viewModel: DashboardViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                
                DashboardScreen(
                    state = state,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onCategorySelect = { viewModel.setCategoryFilter(it) },
                    onPrioritySelect = { viewModel.setPriorityFilter(it) },
                    onSortSelect = { viewModel.setSortOption(it) },
                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onUndoDelete = { viewModel.undoDelete() },
                    onAddTaskClick = { navigateTo(AddTaskRoute()) },
                    onTaskClick = { taskId -> navigateTo(TaskDetailsRoute(taskId)) },
                    onLogoutClick = {
                        scope.launch {
                            authRepository.logout()
                            navigateTo(LoginRoute, clearBackStack = true)
                        }
                    },
                    onSettingsClick = { navigateTo(SettingsRoute) },
                    onCalendarClick = { navigateTo(CalendarRoute) },
                    onAnalyticsClick = { navigateTo(AnalyticsRoute) }
                )
            }
            entry<AddTaskRoute> { route ->
                val viewModel: AddTaskViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                LaunchedEffect(route.initialDateMillis) {
                    if (route.initialDateMillis != null) {
                        viewModel.onDueDateChange(route.initialDateMillis)
                    }
                }

                AddTaskScreen(
                    state = state,
                    onTitleChange = { viewModel.onTitleChange(it) },
                    onDescriptionChange = { viewModel.onDescriptionChange(it) },
                    onCategoryChange = { viewModel.onCategoryChange(it) },
                    onPriorityChange = { viewModel.onPriorityChange(it) },
                    onDueDateChange = { viewModel.onDueDateChange(it) },
                    onDueTimeChange = { viewModel.onDueTimeChange(it) },
                    onReminderToggle = { viewModel.onReminderToggle(it) },
                    onSaveClick = { viewModel.saveTask() },
                    onBackClick = { popBack() }
                )
            }
            entry<EditTaskRoute> { route ->
                val viewModel: EditTaskViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                EditTaskScreen(
                    state = state,
                    onTitleChange = { viewModel.onTitleChange(it) },
                    onDescriptionChange = { viewModel.onDescriptionChange(it) },
                    onCategoryChange = { viewModel.onCategoryChange(it) },
                    onPriorityChange = { viewModel.onPriorityChange(it) },
                    onDueDateChange = { viewModel.onDueDateChange(it) },
                    onDueTimeChange = { viewModel.onDueTimeChange(it) },
                    onReminderToggle = { viewModel.onReminderToggle(it) },
                    onUpdateClick = { viewModel.updateTask() },
                    onBackClick = { popBack() }
                )
            }
            entry<TaskDetailsRoute> { route ->
                val viewModel: TaskDetailsViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                TaskDetailsScreen(
                    state = state,
                    onEditClick = { taskId -> navigateTo(EditTaskRoute(taskId)) },
                    onBackClick = { popBack() }
                )
            }
            entry<CalendarRoute> {
                val viewModel: CalendarViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                CalendarScreen(
                    state = state,
                    onDateSelected = { viewModel.onDateSelected(it) },
                    onMonthChange = { viewModel.onMonthChange(it) },
                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAddTaskClick = { dateMillis -> navigateTo(AddTaskRoute(dateMillis)) },
                    onTaskClick = { taskId -> navigateTo(TaskDetailsRoute(taskId)) },
                    onBackClick = { popBack() }
                )
            }
            entry<AnalyticsRoute> {
                val viewModel: AnalyticsViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                AnalyticsScreen(
                    state = state,
                    onBackClick = { popBack() }
                )
            }
            entry<SettingsRoute> {
                val viewModel: SettingsViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                SettingsScreen(
                    state = state,
                    onThemeSelect = { viewModel.setTheme(it) },
                    onNotificationsToggle = { viewModel.setNotificationsEnabled(it) },
                    onBackupTasks = { uri, onSuccess, onError -> viewModel.backupTasks(uri, onSuccess, onError) },
                    onRestoreTasks = { uri, onSuccess, onError -> viewModel.restoreTasks(uri, onSuccess, onError) },
                    onExportTasks = { uri, onSuccess, onError -> viewModel.exportTasksToCsv(uri, onSuccess, onError) },
                    onClearTasksClick = { viewModel.clearAllTasks() },
                    onLogoutClick = {
                        viewModel.logout()
                        navigateTo(LoginRoute, clearBackStack = true)
                    },
                    onBackClick = { popBack() }
                )
            }
        }
    )
}
