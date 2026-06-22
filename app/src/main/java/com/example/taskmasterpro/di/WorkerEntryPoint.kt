package com.example.taskmasterpro.di

import com.example.taskmasterpro.data.local.database.AppDatabase
import com.example.taskmasterpro.data.preferences.SessionManager
import com.example.taskmasterpro.data.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun taskRepository(): TaskRepository
    fun appDatabase(): AppDatabase
    fun sessionManager(): SessionManager
}
