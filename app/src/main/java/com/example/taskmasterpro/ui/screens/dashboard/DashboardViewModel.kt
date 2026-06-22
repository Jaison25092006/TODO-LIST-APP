package com.example.taskmasterpro.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val totalTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val todayTasksCount: Int = 0,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedPriority: TaskPriority? = null,
    val sortOption: SortOption = SortOption.DUE_DATE,
    val userEmail: String? = null
)

enum class SortOption {
    DUE_DATE,
    PRIORITY,
    TITLE
}

private data class FilterParams(
    val userId: String?,
    val email: String?,
    val search: String,
    val category: String?,
    val priority: TaskPriority?,
    val sort: SortOption
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedPriority = MutableStateFlow<TaskPriority?>(null)
    private val _sortOption = MutableStateFlow(SortOption.DUE_DATE)

    private var recentlyDeletedTask: Task? = null

    val uiState: StateFlow<DashboardUiState> = combine(
        authRepository.currentUserId,
        authRepository.currentUserEmail,
        _searchQuery,
        _selectedCategory,
        _selectedPriority,
        _sortOption
    ) { flows ->
        FilterParams(
            userId = flows[0] as String?,
            email = flows[1] as String?,
            search = flows[2] as String,
            category = flows[3] as String?,
            priority = flows[4] as TaskPriority?,
            sort = flows[5] as SortOption
        )
    }.flatMapLatest { params ->
        val userId = params.userId
        val email = params.email
        val search = params.search
        val category = params.category
        val priority = params.priority
        val sort = params.sort

        if (userId == null) {
            flowOf(DashboardUiState(isLoading = false, userEmail = email))
        } else {
            taskRepository.getTasks(userId).map { allTasks ->
                val filteredTasks = allTasks.filter { task ->
                    val matchesSearch = task.title.contains(search, ignoreCase = true) ||
                            task.description.contains(search, ignoreCase = true)
                    val matchesCategory = category == null || task.category.equals(category, ignoreCase = true)
                    val matchesPriority = priority == null || task.priority == priority
                    matchesSearch && matchesCategory && matchesPriority
                }.let { list ->
                    when (sort) {
                        SortOption.DUE_DATE -> list.sortedBy { it.dueDate }
                        SortOption.PRIORITY -> list.sortedWith(compareByDescending { it.priority.ordinal })
                        SortOption.TITLE -> list.sortedBy { it.title }
                    }
                }

                val now = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000
                val todayStart = now - (now % oneDayMillis)
                val todayEnd = todayStart + oneDayMillis

                DashboardUiState(
                    isLoading = false,
                    tasks = filteredTasks,
                    totalTasksCount = allTasks.size,
                    completedTasksCount = allTasks.count { it.status == TaskStatus.COMPLETED },
                    pendingTasksCount = allTasks.count { it.status == TaskStatus.PENDING },
                    todayTasksCount = allTasks.count { it.dueDate in todayStart..todayEnd },
                    searchQuery = search,
                    selectedCategory = category,
                    selectedPriority = priority,
                    sortOption = sort,
                    userEmail = email
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _selectedPriority.value = priority
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                status = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            )
            taskRepository.updateTask(updatedTask)
        }
    }

    fun deleteTask(task: Task) {
        recentlyDeletedTask = task
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun undoDelete() {
        val taskToRestore = recentlyDeletedTask ?: return
        viewModelScope.launch {
            taskRepository.insertTask(taskToRestore)
            recentlyDeletedTask = null
        }
    }
}
