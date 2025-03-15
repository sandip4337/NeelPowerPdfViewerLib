package com.example.powerpdflibrary

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfDownloader(private val context: Context) {

    private val apiService = RetrofitClient.apiService
    private val filesDirPath = context.filesDir.absolutePath

    fun downloadAndProcessPdf(
        pdfUrl: String,
        pdfName: String,
        pdfId: String,
        onProgressUpdate: (Int) -> Unit,
        onDownloadComplete: (Boolean, String?) -> Unit
    ) {
        if (pdfUrl.isEmpty() || pdfName.isEmpty() || pdfId.isEmpty()) {
            onDownloadComplete(false, "Error: Missing PDF information")
            return
        }

        val htmlFilePath = "$filesDirPath/$pdfName$pdfId.html"
        if (File(htmlFilePath).exists()) {
            onDownloadComplete(true, htmlFilePath)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseBody = async { apiService.downloadPdf(pdfUrl) }.await()
                val contentType = responseBody.contentType()?.toString()

                if (contentType == null || !contentType.contains("pdf", ignoreCase = true)) {
                    withContext(Dispatchers.Main) {
                        onDownloadComplete(false, "Error: This is NOT a PDF file!")
                    }
                    return@launch
                }

                val pdfFile = File(filesDirPath, "$pdfName.pdf")
                val base64File = File(filesDirPath, "base64.txt")
                val htmlFile = File(filesDirPath, "$pdfName$pdfId.html")

                val savePdfDeferred = async {
                    writeResponseBodyToDisk(pdfFile, responseBody, responseBody.contentLength(), onProgressUpdate)
                }
                if (!savePdfDeferred.await()) {
                    withContext(Dispatchers.Main) { onDownloadComplete(false, null) }
                    return@launch
                }

                val encodeBase64Deferred = async { encodeFileToBase64(pdfFile, base64File) }
                encodeBase64Deferred.await()

                val buildHtmlDeferred = async {  buildHtmlContent(base64File, htmlFile) }
                buildHtmlDeferred.await()

                base64File.delete()
                pdfFile.delete()

                withContext(Dispatchers.Main) { onDownloadComplete(true, htmlFile.absolutePath) }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onDownloadComplete(false, "Download failed: ${e.message}")
                }
            }
        }
    }

    private fun writeResponseBodyToDisk(
        file: File,
        body: ResponseBody,
        totalSize: Long,
        onProgressUpdate: (Int) -> Unit
    ): Boolean {
        return try {
            var downloadedSize = 0L
            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(16 * 1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead

                        if (totalSize > 0) { // Prevent division by zero
                            val progress = ((downloadedSize * 100) / totalSize).toInt()
                            onProgressUpdate(progress)
                        }
                    }
                    outputStream.flush()
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun encodeFileToBase64(file: File, outputBase64File: File) = withContext(Dispatchers.IO) {
        outputBase64File.outputStream().use { outputStream ->
            android.util.Base64OutputStream(outputStream, android.util.Base64.NO_WRAP).use { base64Output ->
                file.inputStream().use { input ->
                    val buffer = ByteArray(8 * 1024) // 8 KB buffer
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        base64Output.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }

    private suspend fun buildHtmlContent(base64File: File, outputHtmlFile: File) = withContext(Dispatchers.IO) {
        outputHtmlFile.outputStream().bufferedWriter().use { writer ->
            context.assets.open("pdf_html_viewer_1.html").bufferedReader().use { reader ->
                writer.write(reader.readText()) // Write HTML start part
            }

            base64File.inputStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    writer.write(line) // Write Base64 line-by-line (no large memory usage)
                }
            }

            context.assets.open("pdf_html_viewer_2.html").bufferedReader().use { reader ->
                writer.write(reader.readText()) // Write HTML end part
            }
        }
    }
}
