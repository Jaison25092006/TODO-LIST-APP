package com.example.taskmasterpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY dueDate ASC, dueTime ASC")
    fun getTasksForUser(userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND id = :taskId")
    suspend fun getTaskById(userId: String, taskId: Long): Task?

    @Query("SELECT * FROM tasks WHERE userId = :userId AND category = :category ORDER BY dueDate ASC")
    fun getTasksByCategory(userId: String, category: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND priority = :priority ORDER BY dueDate ASC")
    fun getTasksByPriority(userId: String, priority: TaskPriority): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueDate = :date ORDER BY dueTime ASC")
    fun getTasksByDate(userId: String, date: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = :status ORDER BY dueDate ASC")
    fun getTasksByStatus(userId: String, status: TaskStatus): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND title LIKE '%' || :query || '%' ORDER BY dueDate ASC")
    fun searchTasks(userId: String, query: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task): Int

    @Delete
    suspend fun deleteTask(task: Task): Int

    @Query("DELETE FROM tasks WHERE userId = :userId AND id = :taskId")
    suspend fun deleteTaskById(userId: String, taskId: Long): Int

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllTasksForUser(userId: String): Int
}
