package com.example.taskmasterpro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskMasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Notification Channels
        com.example.taskmasterpro.notification.NotificationHelper.createNotificationChannels(this)
        // Schedule Periodic Tasks
        com.example.taskmasterpro.notification.NotificationHelper.scheduleDailySummaryWorker(this)
    }
}
