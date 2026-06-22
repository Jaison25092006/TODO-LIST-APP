package com.example.taskmasterpro.ui.screens.addtask

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

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val category: String = "Work",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Long? = null,
    val dueTime: String = "12:00 PM",
    val isReminderEnabled: Boolean = false,
    val titleError: String? = null,
    val dateError: String? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState

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

    fun saveTask() {
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
                        userId = userId,
                        title = currentState.title.trim(),
                        description = currentState.description.trim(),
                        category = currentState.category,
                        priority = currentState.priority,
                        dueDate = currentState.dueDate!!,
                        dueTime = currentState.dueTime,
                        status = TaskStatus.PENDING
                    )
                    val insertedId = taskRepository.insertTask(task)
                    if (currentState.isReminderEnabled) {
                        com.example.taskmasterpro.notification.NotificationHelper.scheduleTaskReminder(
                            context,
                            task.copy(id = insertedId)
                        )
                    }
                    _uiState.value = currentState.copy(isLoading = false, isSaved = true)
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to save task"
                )
            }
        }
    }
}
