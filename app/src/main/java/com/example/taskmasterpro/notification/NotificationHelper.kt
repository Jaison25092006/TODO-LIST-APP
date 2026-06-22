package com.example.taskmasterpro.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.taskmasterpro.data.local.entity.Task
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val REMINDER_CHANNEL_ID = "task_reminders_channel"
    const val SUMMARY_CHANNEL_ID = "daily_summaries_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task due reminders"
                enableLights(true)
                enableVibration(true)
            }

            val summaryChannel = NotificationChannel(
                SUMMARY_CHANNEL_ID,
                "Daily Summary & Overdue Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications summarizing tasks due today and overdue alerts"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(summaryChannel)
        }
    }

    fun scheduleTaskReminder(context: Context, task: Task) {
        val delay = task.dueDate - System.currentTimeMillis()
        if (delay <= 0) return

        val inputData = workDataOf(
            "taskId" to task.id,
            "title" to task.title,
            "description" to task.description,
            "userId" to task.userId
        )

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("task_reminder_${task.id}")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "task_reminder_${task.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("task_reminder_$taskId")
    }

    fun scheduleDailySummaryWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailyTaskSummaryWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_summary_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
