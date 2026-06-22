package com.example.taskmasterpro.di

import com.example.taskmasterpro.data.repository.AuthRepository
import com.example.taskmasterpro.data.repository.SimpleLocalAuthRepositoryImpl
import com.example.taskmasterpro.data.repository.TaskRepository
import com.example.taskmasterpro.data.repository.TaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        simpleLocalAuthRepositoryImpl: SimpleLocalAuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}
