package com.example.taskmasterpro.data.repository

import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(userId: String): Flow<List<Task>>
    suspend fun getTaskById(userId: String, taskId: Long): Task?
    fun getTasksByCategory(userId: String, category: String): Flow<List<Task>>
    fun getTasksByPriority(userId: String, priority: TaskPriority): Flow<List<Task>>
    fun getTasksByDate(userId: String, date: Long): Flow<List<Task>>
    fun getTasksByStatus(userId: String, status: TaskStatus): Flow<List<Task>>
    fun searchTasks(userId: String, query: String): Flow<List<Task>>
    
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task): Boolean
    suspend fun deleteTask(task: Task): Boolean
    suspend fun deleteTaskById(userId: String, taskId: Long): Boolean
    suspend fun deleteAllTasks(userId: String): Boolean
}
