package com.example.powerpdflibrary

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var fileNotPdf: TextView
    private var pdfUrl: String? = null
    private var pdfName: String? = null
    private var pdfId: String? = null
    private var isDownload: Boolean? = false
    private var progressDialog: PdfProgressDialog? = null
    private lateinit var filesDirPath: String
    private val apiService = RetrofitClient.apiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        webView = findViewById(R.id.webView)
        fileNotPdf = findViewById(R.id.fileNotPdf)

        pdfName = intent.getStringExtra("PDF_NAME") ?: ""
        pdfUrl = intent.getStringExtra("PDF_URL") ?: ""
        pdfId = intent.getStringExtra("PDF_ID") ?: ""
        isDownload = intent.getBooleanExtra("isDownload", false)


        if (pdfUrl?.isNotEmpty() == true && pdfName?.isNotEmpty() == true && pdfId?.isNotEmpty() == true) {
            filesDirPath = filesDir.absolutePath
            val htmlFilePath = "$filesDirPath/$pdfName$pdfId.html"

            showProgressDialog(true)
            lifecycleScope.launch {
                if (File(htmlFilePath).exists()) {
                    pdfOpenInWebView(htmlFilePath)
                } else {
                    downloadAndProcessPdf(pdfUrl!!, pdfName!!, pdfId!!)
                }
            }
        } else {
            fileNotPdf.text = "Sorry!!! Pdf information is missing"
            fileNotPdf.visibility = View.VISIBLE
        }
    }

    private suspend fun downloadAndProcessPdf(url: String, pdfName: String, pdfId: String) {
        try {
            val responseBody = withContext(Dispatchers.IO) { apiService.downloadPdf(url) }
            val contentType = responseBody.contentType()?.toString()
            Log.d("Download", "Content-Type: $contentType")

            if (contentType == null || !contentType.contains("pdf", ignoreCase = true)) {
                Log.e("Download", "Error: This is NOT a PDF file!")
                hideProgressDialog()
                withContext(Dispatchers.Main) { fileNotPdf.visibility = View.VISIBLE }
                return
            }

            val pdfFile = File(filesDir, "$pdfName.pdf").apply {
                setReadable(false, false) // Prevents external access
                setWritable(false, false)
            }
            val base64File = File(filesDir, "base64.txt")
            val htmlFile = File(filesDir, "$pdfName$pdfId.html")

            coroutineScope {
                val savePdfDeferred = async { writeResponseBodyToDisk(pdfFile, responseBody) }
                val savePdfSuccess = savePdfDeferred.await()
                if (!savePdfSuccess) {
                    Log.e("Download", "Failed to download file")
                    return@coroutineScope
                }
                Log.d("Download", "File downloaded successfully: ${pdfFile.length()} bytes")

                val encodeBase64Deferred = async { encodeFileToBase64(pdfFile) }
                val base64String = encodeBase64Deferred.await()
                base64File.writeText(base64String)

                val htmlPart1 = async { assets.open("pdf_html_viewer_1.html").bufferedReader().use { it.readText() } }
                val htmlPart2 = async { assets.open("pdf_html_viewer_2.html").bufferedReader().use { it.readText() } }

                htmlFile.writeText(htmlPart1.await() + base64File.readText() + htmlPart2.await())

                base64File.delete()
                pdfFile.delete()
            }
            if (isDownload == false) {
                withContext(Dispatchers.Main) { pdfOpenInWebView(htmlFile.absolutePath) }
            }
        } catch (e: IOException) {
            Log.e("DownloadError", "Error downloading PDF: ${e.message}", e)
        }
    }

    private suspend fun writeResponseBodyToDisk(file: File, body: ResponseBody): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                body.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(16 * 1024)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
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
    }

    private suspend fun encodeFileToBase64(file: File): String {
        return withContext(Dispatchers.IO) {
            val bytes = file.readBytes()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(bytes)
            } else {
                android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            }
        }
    }

    private fun pdfOpenInWebView(filePath: String) {
        webView.settings.apply {
            allowFileAccess = true
            javaScriptEnabled = true
            allowContentAccess = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, pdf_file: String) {
                hideProgressDialog()
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                Log.e("WebView", "Error: ${error?.description}")
            }
        }
        webView.loadUrl("file:///$filePath")
    }

    fun showProgressDialog(isDismissOnBack: Boolean = true) {
        if (progressDialog == null) {
            progressDialog = PdfProgressDialog(context = this, isDismissOnBack)
        }
        progressDialog?.show()
    }

    fun hideProgressDialog() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    companion object {
        fun openPdfViewer(context: Context, pdfUrl: String, pdfName: String, pdfId: String) {
            val intent = Intent(context, PdfViewerActivity::class.java).apply {
                putExtra("PDF_URL", pdfUrl)
                putExtra("PDF_NAME", pdfName)
                putExtra("PDF_ID", pdfId)
            }
            context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
