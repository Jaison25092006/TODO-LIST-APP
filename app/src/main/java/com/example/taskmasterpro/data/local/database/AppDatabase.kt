package com.example.taskmasterpro.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskmasterpro.data.local.dao.TaskDao
import com.example.taskmasterpro.data.local.dao.UserDao
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.local.entity.User

@Database(entities = [Task::class, User::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "taskmaster_pro_db"
    }
}
