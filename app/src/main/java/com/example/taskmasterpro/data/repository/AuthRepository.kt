package com.example.taskmasterpro.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserEmail: Flow<String?>
    val currentUserId: Flow<String?>
    val isLoggedIn: Flow<Boolean>

    suspend fun register(email: String, password: String): Result<String>
    suspend fun login(email: String, password: String, rememberMe: Boolean = false): Result<String>
    suspend fun logout(): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}
