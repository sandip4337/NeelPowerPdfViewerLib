package com.example.powerpdflibrary

import android.content.Context
import android.util.Log
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

        val apiService = RetrofitClient.getApiService(baseUrl)

        return try {
            val responseBody = apiService.downloadPdf(pdfUrl)
            val success = writeResponseBodyToDisk(responseBody, pdfFile, pdfId)

            if (!success) return Result.failure()

            // Convert PDF to Base64 and generate HTML file
            val base64File = File(filesDirPath, "base64_$pdfId.txt")
            val htmlFile = File(filesDirPath, "$pdfName$pdfId.html")

            encodeFileToBase64(pdfFile, base64File)
            buildHtmlContent(base64File, htmlFile)

            // Clean up temporary files
            base64File.delete()
            pdfFile.delete()

            Log.d("PdfDownloadWorker", "Download complete: ${htmlFile.absolutePath}")

            Result.success(workDataOf("HTML_FILE_PATH" to htmlFile.absolutePath))

        } catch (e: IOException) {
            Log.e("PdfDownloadWorker", "Download failed: ${e.message}")
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
                                Log.d("PdfDownloadWorker", "Downloading: $progress% ($pdfId)")
                            }
                        }
                    }
                }
                return@withContext true
            } catch (e: IOException) {
                Log.e("PdfDownloadWorker", "Error writing file: ${e.message}")
                return@withContext false
            }
        }

    /**
     * Converts PDF to Base64 and writes to a file.
     */
    private suspend fun encodeFileToBase64(file: File, outputBase64File: File) =
        withContext(Dispatchers.IO) {
            try {
                outputBase64File.outputStream().use { outputStream ->
                    android.util.Base64OutputStream(outputStream, android.util.Base64.NO_WRAP).use { base64Output ->
                        file.inputStream().use { input ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                base64Output.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                }
                Log.d("PdfDownloadWorker", "Base64 encoding complete: ${outputBase64File.absolutePath}")
            } catch (e: IOException) {
                Log.e("PdfDownloadWorker", "Base64 encoding failed: ${e.message}")
            }
        }

    /**
     * Builds the HTML file using the Base64 encoded data.
     */
    private suspend fun buildHtmlContent(base64File: File, outputHtmlFile: File) =
        withContext(Dispatchers.IO) {
            try {
                outputHtmlFile.outputStream().use { outputStream ->
                    // Write first HTML part
                    applicationContext.assets.open("pdf_html_viewer_1.html").use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    // Stream base64 data in chunks instead of loading into memory
                    base64File.inputStream().buffered().use { input ->
                        val buffer = ByteArray(32 * 1024) // 32KB Buffer
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }

                    // Write second HTML part
                    applicationContext.assets.open("pdf_html_viewer_2.html").use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Log.d("PdfDownloadWorker", "HTML file created: ${outputHtmlFile.absolutePath}")
            } catch (e: IOException) {
                Log.e("PdfDownloadWorker", "HTML file creation failed: ${e.message}")
            }
        }
}
