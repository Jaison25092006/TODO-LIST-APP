package com.example.taskmasterpro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: TaskPriority,
    val dueDate: Long,
    val dueTime: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
