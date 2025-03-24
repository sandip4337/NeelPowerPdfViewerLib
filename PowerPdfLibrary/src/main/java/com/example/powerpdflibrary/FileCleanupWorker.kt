package com.example.powerpdflibrary

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.util.concurrent.TimeUnit

class FileCleanupWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("PowerPdfLibraryFilePrefs", Context.MODE_PRIVATE)
        val filePaths = sharedPreferences.getStringSet("filePaths", emptySet())?.toMutableSet() ?: return Result.success()

        for (filePath in filePaths) {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        }

        // Clear file paths from SharedPreferences after deletion
        sharedPreferences.edit().remove("filePaths").apply()
        return Result.success()
    }

    companion object {
        fun scheduleFileDeletion(context: Context) {
            val workManager = WorkManager.getInstance(context)
            val cleanupRequest = PeriodicWorkRequestBuilder<FileCleanupWorker>(6, TimeUnit.HOURS)
                .build()
            workManager.enqueue(cleanupRequest)
        }
    }
}
