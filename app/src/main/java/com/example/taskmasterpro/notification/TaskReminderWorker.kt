package com.example.taskmasterpro.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmasterpro.data.local.entity.Task
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.di.WorkerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("taskId", -1L)
        val title = inputData.getString("title") ?: "Task Reminder"
        val desc = inputData.getString("description") ?: ""
        val userId = inputData.getString("userId") ?: ""

        if (taskId == -1L || userId.isEmpty()) return Result.failure()

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            WorkerEntryPoint::class.java
        )
        
        // Check if notifications are enabled
        val sessionManager = entryPoint.sessionManager()
        val notificationsEnabled = sessionManager.notificationsEnabledFlow.first()
        if (!notificationsEnabled) {
            return Result.success()
        }

        val repository = entryPoint.taskRepository()
        val task = repository.getTaskById(userId, taskId)

        if (task != null && task.status == TaskStatus.PENDING) {
            sendNotification(taskId.toInt(), title, desc)
        }

        return Result.success()
    }


    private fun sendNotification(id: Int, title: String, content: String) {
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(id, notification)
        } catch (e: SecurityException) {
            // Safe handling if notifications permissions are missing
        }
    }
}
