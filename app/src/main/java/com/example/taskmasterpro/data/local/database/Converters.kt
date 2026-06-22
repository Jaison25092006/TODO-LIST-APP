package com.example.taskmasterpro.data.local.database

import androidx.room.TypeConverter
import com.example.taskmasterpro.data.model.TaskPriority
import com.example.taskmasterpro.data.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): TaskPriority {
        return TaskPriority.valueOf(value)
    }

    @TypeConverter
    fun fromStatus(status: TaskStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): TaskStatus {
        return TaskStatus.valueOf(value)
    }
}
