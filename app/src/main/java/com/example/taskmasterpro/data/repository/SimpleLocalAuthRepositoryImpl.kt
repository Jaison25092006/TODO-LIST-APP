package com.example.taskmasterpro.data.repository

import com.example.taskmasterpro.data.preferences.SessionManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleLocalAuthRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {

    companion object {
        private const val USERS_KEY_PREFIX = "user_pwd_"
    }

    override val currentUserId: Flow<String?> = sessionManager.userIdFlow.map { id ->
        if (id == -1L) null else id.toString()
    }

    override val currentUserEmail: Flow<String?> = sessionManager.userNameFlow.map { name ->
        if (name == "Guest") null else name
    }

    override val isLoggedIn: Flow<Boolean> = sessionManager.userIdFlow.map { id ->
        id != -1L
    }

    override suspend fun register(email: String, password: String): Result<String> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password cannot be empty"))
        }
        return try {
            val key = stringPreferencesKey(USERS_KEY_PREFIX + email)
            dataStore.edit { preferences ->
                preferences[key] = password
            }
            // Auto-login after successful registration
            val mockUserId = Math.abs(email.hashCode().toLong())
            sessionManager.saveSession(mockUserId, email)
            Result.success(mockUserId.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String, rememberMe: Boolean): Result<String> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email and password cannot be empty"))
        }
        return try {
            val key = stringPreferencesKey(USERS_KEY_PREFIX + email)
            val storedPassword = dataStore.data.map { preferences ->
                preferences[key]
            }.first()

            if (storedPassword == null) {
                // For a seamless test experience, auto-register new credentials
                val mockUserId = Math.abs(email.hashCode().toLong())
                dataStore.edit { preferences ->
                    preferences[key] = password
                }
                sessionManager.saveSession(mockUserId, email)
                Result.success(mockUserId.toString())
            } else if (storedPassword == password) {
                val mockUserId = Math.abs(email.hashCode().toLong())
                sessionManager.saveSession(mockUserId, email)
                Result.success(mockUserId.toString())
            } else {
                Result.failure(Exception("Incorrect password for this email"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return Result.success(Unit)
    }
}
