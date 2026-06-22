package com.example.taskmasterpro.data.repository

import com.example.taskmasterpro.data.local.dao.TaskDao
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasks(userId: String): Flow<List<Task>> {
        return taskDao.getTasksForUser(userId)
    }

    override suspend fun getTaskById(userId: String, taskId: Long): Task? {
        return taskDao.getTaskById(userId, taskId)
    }

    override fun getTasksByCategory(userId: String, category: String): Flow<List<Task>> {
        return taskDao.getTasksByCategory(userId, category)
    }

    override fun getTasksByPriority(userId: String, priority: TaskPriority): Flow<List<Task>> {
        return taskDao.getTasksByPriority(userId, priority)
    }

    override fun getTasksByDate(userId: String, date: Long): Flow<List<Task>> {
        return taskDao.getTasksByDate(userId, date)
    }

    override fun getTasksByStatus(userId: String, status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatus(userId, status)
    }

    override fun searchTasks(userId: String, query: String): Flow<List<Task>> {
        return taskDao.searchTasks(userId, query)
    }

    override suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: Task): Boolean {
        return taskDao.updateTask(task) > 0
    }

    override suspend fun deleteTask(task: Task): Boolean {
        return taskDao.deleteTask(task) > 0
    }

    override suspend fun deleteTaskById(userId: String, taskId: Long): Boolean {
        return taskDao.deleteTaskById(userId, taskId) > 0
    }

    override suspend fun deleteAllTasks(userId: String): Boolean {
        return taskDao.deleteAllTasksForUser(userId) > 0
    }
}
