package com.example.taskmasterpro.ui.screens.edittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTaskUiState(
    val taskId: Long = 0L,
    val title: String = "",
    val description: String = "",
    val category: String = "Work",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Long? = null,
    val dueTime: String = "12:00 PM",
    val isReminderEnabled: Boolean = false,
    val status: TaskStatus = TaskStatus.PENDING,
    val titleError: String? = null,
    val dateError: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTaskUiState())
    val uiState: StateFlow<EditTaskUiState> = _uiState

    init {
        val id: Long? = savedStateHandle["taskId"]
        if (id != null && id > 0L) {
            loadTask(id)
        }
    }

    fun loadTask(taskId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, taskId = taskId)
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUserId.first()
                if (userId != null) {
                    val task = taskRepository.getTaskById(userId, taskId)
                    if (task != null) {
                        _uiState.value = EditTaskUiState(
                            taskId = task.id,
                            title = task.title,
                            description = task.description,
                            category = task.category,
                            priority = task.priority,
                            dueDate = task.dueDate,
                            dueTime = task.dueTime,
                            status = task.status,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Task not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load task"
                )
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle, titleError = null)
    }

    fun onDescriptionChange(newDesc: String) {
        _uiState.value = _uiState.value.copy(description = newDesc)
    }

    fun onCategoryChange(newCategory: String) {
        _uiState.value = _uiState.value.copy(category = newCategory)
    }

    fun onPriorityChange(newPriority: TaskPriority) {
        _uiState.value = _uiState.value.copy(priority = newPriority)
    }

    fun onDueDateChange(dateMillis: Long?) {
        _uiState.value = _uiState.value.copy(dueDate = dateMillis, dateError = null)
    }

    fun onDueTimeChange(newTime: String) {
        _uiState.value = _uiState.value.copy(dueTime = newTime)
    }

    fun onReminderToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isReminderEnabled = enabled)
    }

    fun updateTask() {
        val currentState = _uiState.value
        var hasError = false
        var titleErr: String? = null
        var dateErr: String? = null

        if (currentState.title.isBlank()) {
            titleErr = "Title is required"
            hasError = true
        }

        if (currentState.dueDate == null) {
            dateErr = "Due date is required"
            hasError = true
        }

        if (hasError) {
            _uiState.value = currentState.copy(titleError = titleErr, dateError = dateErr)
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val userId = authRepository.currentUserId.first()
                if (userId != null) {
                    val task = Task(
                        id = currentState.taskId,
                        userId = userId,
                        title = currentState.title.trim(),
                        description = currentState.description.trim(),
                        category = currentState.category,
                        priority = currentState.priority,
                        dueDate = currentState.dueDate!!,
                        dueTime = currentState.dueTime,
                        status = currentState.status
                    )
                    val updated = taskRepository.updateTask(task)
                    if (updated) {
                        if (currentState.isReminderEnabled) {
                            com.example.taskmasterpro.notification.NotificationHelper.scheduleTaskReminder(context, task)
                        } else {
                            com.example.taskmasterpro.notification.NotificationHelper.cancelTaskReminder(context, task.id)
                        }
                        _uiState.value = currentState.copy(isLoading = false, isSaved = true)
                    } else {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = "Failed to update task"
                        )
                    }
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to update task"
                )
            }
        }
    }
}
