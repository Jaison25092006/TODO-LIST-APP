package com.example.taskmasterpro.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute : NavKey

@Serializable
object LoginRoute : NavKey

@Serializable
object RegisterRoute : NavKey

@Serializable
object DashboardRoute : NavKey

@Serializable
data class AddTaskRoute(val initialDateMillis: Long? = null) : NavKey

@Serializable
data class EditTaskRoute(val taskId: Long) : NavKey

@Serializable
data class TaskDetailsRoute(val taskId: Long) : NavKey

@Serializable
object CalendarRoute : NavKey

@Serializable
object AnalyticsRoute : NavKey

@Serializable
object SettingsRoute : NavKey
