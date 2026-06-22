package com.example.taskmasterpro.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CalendarUiState(
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val currentMonth: Calendar = Calendar.getInstance(),
    val allTasks: List<Task> = emptyList(),
    val selectedDateTasks: List<Task> = emptyList(),
    val taskMarkers: Map<Long, List<Task>> = emptyMap(),
    val userEmail: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _currentMonth = MutableStateFlow(Calendar.getInstance())

    val uiState: StateFlow<CalendarUiState> = combine(
        authRepository.currentUserId,
        authRepository.currentUserEmail,
        _selectedDate,
        _currentMonth
    ) { userId, email, selectedDate, currentMonth ->
        userId to (email to (selectedDate to currentMonth))
    }.flatMapLatest { pair ->
        val userId = pair.first
        val email = pair.second.first
        val selectedDate = pair.second.second.first
        val currentMonth = pair.second.second.second

        if (userId == null) {
            flowOf(CalendarUiState(userEmail = email))
        } else {
            taskRepository.getTasks(userId).map { tasks ->
                val markers = tasks.groupBy { task ->
                    getStartOfDay(task.dueDate)
                }

                val selectedDateStart = getStartOfDay(selectedDate)
                val selectedDateTasks = markers[selectedDateStart] ?: emptyList()

                CalendarUiState(
                    selectedDateMillis = selectedDate,
                    currentMonth = currentMonth,
                    allTasks = tasks,
                    selectedDateTasks = selectedDateTasks,
                    taskMarkers = markers,
                    userEmail = email
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun onDateSelected(millis: Long) {
        _selectedDate.value = millis
    }

    fun onMonthChange(change: Int) {
        val nextMonth = (_currentMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, change)
        }
        _currentMonth.value = nextMonth
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(
                status = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            )
            taskRepository.updateTask(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    private fun getStartOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
