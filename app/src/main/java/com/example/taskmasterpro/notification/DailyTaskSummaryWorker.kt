package com.example.taskmasterpro.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskmasterpro.data.model.TaskStatus
import com.example.taskmasterpro.di.WorkerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class DailyTaskSummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
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

        val database = entryPoint.appDatabase()
        
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000
        val todayStart = now - (now % oneDayMillis)
        val todayEnd = todayStart + oneDayMillis

        val db = database.openHelper.readableDatabase
        
        var dueTodayCount = 0
        var overdueCount = 0

        val cursor = db.query("SELECT dueDate, status FROM tasks")
        if (cursor.moveToFirst()) {
            do {
                val dueDate = cursor.getLong(cursor.getColumnIndexOrThrow("dueDate"))
                val statusStr = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                
                if (statusStr == TaskStatus.PENDING.name) {
                    if (dueDate in todayStart..todayEnd) {
                        dueTodayCount++
                    } else if (dueDate < todayStart) {
                        overdueCount++
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (dueTodayCount > 0) {
            sendNotification(
                1001,
                "Tasks Due Today",
                "You have $dueTodayCount task(s) due today! Stay productive."
            )
        }

        if (overdueCount > 0) {
            sendNotification(
                1002,
                "Overdue Tasks Alert",
                "You have $overdueCount overdue task(s) that need your attention!"
            )
        }

        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, content: String) {
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.SUMMARY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(id, notification)
        } catch (e: SecurityException) {
            // Safe handling if notifications permissions are missing
        }
    }
}
