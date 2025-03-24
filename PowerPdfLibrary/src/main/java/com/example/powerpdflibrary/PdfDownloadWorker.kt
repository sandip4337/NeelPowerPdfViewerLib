package com.example.powerpdflibrary

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.*

class PdfDownloadWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val filesDirPath = context.filesDir.absolutePath

    override suspend fun doWork(): Result {
        val pdfUrl = inputData.getString("PDF_URL") ?: return Result.failure()
        val pdfName = inputData.getString("PDF_NAME") ?: return Result.failure()
        val pdfId = inputData.getString("PDF_ID") ?: return Result.failure()
        val baseUrl = inputData.getString("BASE_URL") ?: return Result.failure()

        val pdfFile = File(filesDirPath, "$pdfName.pdf")

        val base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
        if (File(base46FilePath).exists()) return Result.success()

        val apiService = RetrofitClient.getApiService(baseUrl)

        return try {
            val responseBody = apiService.downloadPdf(pdfUrl)
            val success = writeResponseBodyToDisk(responseBody, pdfFile, pdfId)

            if (!success) return Result.failure()

            // Convert PDF to Base64
            val base64File = File(filesDirPath, "base64_$pdfName$pdfId.txt")

            val base64Success = encodeFileToBase64(pdfFile, base64File)

            if (!base64Success) return Result.failure()

            // Clean up temporary files
            pdfFile.delete()

            Result.success()
        } catch (e: IOException) {
            Result.failure()
        }
    }

    /**
     * Downloads the file and updates progress.
     */
    private suspend fun writeResponseBodyToDisk(body: ResponseBody, outputFile: File, pdfId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(16 * 1024)
                var bytesRead: Int
                var downloadedBytes = 0L
                val totalBytes = body.contentLength()
                var lastProgress = 0

                inputStream.use { input ->
                    outputStream.use { output ->
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val progress = ((downloadedBytes.toFloat() / totalBytes) * 100).toInt()

                            if (progress >= lastProgress + 2) { // Send updates every 5%
                                setProgressAsync(workDataOf("PROGRESS" to progress))
                                lastProgress = progress
                            }
                        }
                    }
                }
                return@withContext true
            } catch (e: IOException) {
                return@withContext false
            }
        }

    /**
     * Converts PDF to Base64 and writes to a file.
     */
    private suspend fun encodeFileToBase64(file: File, outputBase64File: File): Boolean =
        withContext(Dispatchers.IO) {
            try {
                outputBase64File.outputStream().use { outputStream ->
                    android.util.Base64OutputStream(outputStream, android.util.Base64.NO_WRAP)
                        .use { base64Output ->
                            file.inputStream().use { input ->
                                val buffer = ByteArray(8 * 1024)
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    base64Output.write(buffer, 0, bytesRead)
                                }
                            }
                        }
                }
                return@withContext true
            } catch (e: IOException) {
                return@withContext false
            }
        }
}
