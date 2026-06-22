package com.example.taskmasterpro.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.data.preferences.SessionManager
import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class SettingsUiState(
    val theme: String = "SYSTEM",
    val userEmail: String? = null,
    val notificationsEnabled: Boolean = true,
    val isCleared: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        sessionManager.themeFlow,
        authRepository.currentUserEmail,
        sessionManager.notificationsEnabledFlow
    ) { theme, email, notificationsEnabled ->
        SettingsUiState(
            theme = theme,
            userEmail = email,
            notificationsEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: String) {
        viewModelScope.launch {
            sessionManager.saveThemePreference(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.saveNotificationsEnabled(enabled)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.first()
            if (userId != null) {
                taskRepository.deleteAllTasks(userId)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun backupTasks(uri: Uri, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userIdFlow = authRepository.currentUserId.first()
                if (userIdFlow == null) {
                    onError("User not logged in")
                    return@launch
                }
                
                val tasks = taskRepository.getTasks(userIdFlow).first()
                val jsonArray = JSONArray()
                
                for (task in tasks) {
                    val jsonObject = JSONObject().apply {
                        put("title", task.title)
                        put("description", task.description)
                        put("category", task.category)
                        put("priority", task.priority.name)
                        put("dueDate", task.dueDate)
                        put("dueTime", task.dueTime)
                        put("status", task.status.name)
                        put("createdAt", task.createdAt)
                    }
                    jsonArray.put(jsonObject)
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonArray.toString(4).toByteArray())
                }
                onSuccess(tasks.size)
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error during backup")
            }
        }
    }

    fun restoreTasks(uri: Uri, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userIdFlow = authRepository.currentUserId.first()
                if (userIdFlow == null) {
                    onError("User not logged in")
                    return@launch
                }

                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Failed to read selection file")

                val jsonArray = JSONArray(jsonString)
                var restoredCount = 0
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val task = Task(
                        id = 0L, // auto-generate new unique local ID
                        userId = userIdFlow,
                        title = jsonObject.getString("title"),
                        description = jsonObject.optString("description", ""),
                        category = jsonObject.optString("category", "General"),
                        priority = TaskPriority.valueOf(jsonObject.optString("priority", TaskPriority.MEDIUM.name)),
                        dueDate = jsonObject.getLong("dueDate"),
                        dueTime = jsonObject.optString("dueTime", "12:00 PM"),
                        status = TaskStatus.valueOf(jsonObject.optString("status", TaskStatus.PENDING.name)),
                        createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis())
                    )
                    taskRepository.insertTask(task)
                    restoredCount++
                }
                onSuccess(restoredCount)
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error during restore")
            }
        }
    }

    fun exportTasksToCsv(uri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val userIdFlow = authRepository.currentUserId.first()
                if (userIdFlow == null) {
                    onError("User not logged in")
                    return@launch
                }

                val tasks = taskRepository.getTasks(userIdFlow).first()

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.bufferedWriter().use { writer ->
                        // CSV Header
                        writer.write("Title,Description,Category,Priority,Due Date,Due Time,Status,Created At\n")
                        for (task in tasks) {
                            val titleEscaped = escapeCsvField(task.title)
                            val descEscaped = escapeCsvField(task.description)
                            val categoryEscaped = escapeCsvField(task.category)
                            val priority = task.priority.name
                            val dueDate = task.dueDate.toString()
                            val dueTime = task.dueTime
                            val status = task.status.name
                            val createdAt = task.createdAt.toString()

                            writer.write("$titleEscaped,$descEscaped,$categoryEscaped,$priority,$dueDate,$dueTime,$status,$createdAt\n")
                        }
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error during CSV export")
            }
        }
    }

    private fun escapeCsvField(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        if (!needsQuotes) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }
}

