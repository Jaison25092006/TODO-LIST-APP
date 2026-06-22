package com.example.taskmasterpro.ui.screens.taskdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailsUiState(
    val task: Task? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState

    init {
        val id: Long? = savedStateHandle["taskId"]
        if (id != null && id > 0L) {
            loadTask(id)
        }
    }

    fun loadTask(taskId: Long) {
        _uiState.value = TaskDetailsUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUserId.first()
                if (userId != null) {
                    val task = taskRepository.getTaskById(userId, taskId)
                    _uiState.value = TaskDetailsUiState(task = task, isLoading = false)
                } else {
                    _uiState.value = TaskDetailsUiState(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TaskDetailsUiState(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load task details"
                )
            }
        }
    }
}
