package com.example.taskmasterpro.ui.screens.analytics

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
import java.util.*
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap(),
    val priorityCounts: Map<TaskPriority, Int> = emptyMap(),
    val weeklyProductivity: Map<String, Int> = emptyMap(),
    val monthlyProductivity: Map<Int, Int> = emptyMap(),
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) {
            flowOf(AnalyticsUiState(isLoading = false, errorMessage = "User not logged in"))
        } else {
            taskRepository.getTasks(userId).map { tasks ->
                val completed = tasks.count { it.status == TaskStatus.COMPLETED }
                val pending = tasks.count { it.status == TaskStatus.PENDING }
                val categories = tasks.groupBy { it.category }.mapValues { it.value.size }
                val priorities = tasks.groupBy { it.priority }.mapValues { it.value.size }

                val weekly = calculateWeeklyProductivity(tasks)
                val monthly = calculateMonthlyProductivity(tasks)

                AnalyticsUiState(
                    isLoading = false,
                    completedCount = completed,
                    pendingCount = pending,
                    categoryCounts = categories,
                    priorityCounts = priorities,
                    weeklyProductivity = weekly,
                    monthlyProductivity = monthly
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    private fun calculateWeeklyProductivity(tasks: List<Task>): Map<String, Int> {
        val result = LinkedHashMap<String, Int>()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        val daysList = mutableListOf<Pair<Int, String>>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            val dayName = dayNames[dayOfWeek]
            daysList.add(cal.get(Calendar.DAY_OF_YEAR) to dayName)
            result[dayName] = 0
        }

        tasks.filter { it.status == TaskStatus.COMPLETED }.forEach { task ->
            val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
            val taskDayOfYear = taskCal.get(Calendar.DAY_OF_YEAR)
            daysList.find { it.first == taskDayOfYear }?.let { (_, dayName) ->
                result[dayName] = (result[dayName] ?: 0) + 1
            }
        }
        return result
    }

    private fun calculateMonthlyProductivity(tasks: List<Task>): Map<Int, Int> {
        val result = LinkedHashMap<Int, Int>()
        for (i in 29 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            result[cal.get(Calendar.DAY_OF_MONTH)] = 0
        }

        tasks.filter { it.status == TaskStatus.COMPLETED }.forEach { task ->
            val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
            val diff = System.currentTimeMillis() - task.dueDate
            val diffDays = diff / (24 * 60 * 60 * 1000)
            if (diffDays in 0..29) {
                val dayOfMonth = taskCal.get(Calendar.DAY_OF_MONTH)
                result[dayOfMonth] = (result[dayOfMonth] ?: 0) + 1
            }
        }
        return result
    }
}
