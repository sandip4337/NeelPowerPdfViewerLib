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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        if (pdfUrl?.isNotEmpty() == true && pdfName?.isNotEmpty() == true && pdfId?.isNotEmpty() == true) {
            filesDirPath = filesDir.absolutePath
            val htmlFilePath = "$filesDirPath/$pdfName$pdfId.html"

            if (File(htmlFilePath).exists()) {
                showProgressDialog(true)
                pdfOpenInWebView(htmlFilePath)
            } else {
                lifecycleScope.launch {
                    showProgressDialog(true)
                    downloadPdf(pdfUrl ?: "", pdfName ?: "", pdfId ?: "")
                }
            }
        } else {
            fileNotPdf.text = "Sorry!!! Pdf information is missing"
            fileNotPdf.visibility = View.VISIBLE
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

    private suspend fun downloadPdf(url: String, pdfName: String, pdfId: String) {
        withContext(Dispatchers.IO) {
            try {
                val responseBody = apiService.downloadPdf(url)
                val contentType = responseBody.contentType()?.toString()
                Log.d("Download", "Content-Type: $contentType")

                if (contentType == null || !contentType.contains("pdf", ignoreCase = true)) {
                    Log.e("Download", "Error: This is NOT a PDF file!")
                    hideProgressDialog()
                    withContext(Dispatchers.Main) {
                        fileNotPdf.visibility = View.VISIBLE
                    }
                } else {
                    processPdfDownload(responseBody, pdfName, pdfId)
                }
            } catch (e: IOException) {
                Log.e("DownloadError", "Error downloading PDF: ${e.message}", e)
            }
        }
    }

    private suspend fun processPdfDownload(responseBody: ResponseBody, pdfName: String, pdfId: String?) {
        withContext(Dispatchers.IO) {
            val pdfFile = File(filesDir, "$pdfName.pdf")
            val base64File = File(filesDir, "base64.txt")
            val htmlFile = File(filesDir, "$pdfName$pdfId.html")

            if (writeResponseBodyToDisk(pdfFile, responseBody)) {
                Log.d("Download", "File downloaded successfully: ${pdfFile.length()} bytes")
            } else {
                Log.e("Download", "Failed to download file")
                return@withContext
            }

            val base64String = encodeFileToBase64(pdfFile)
            base64File.writeText(base64String)

            val htmlPart1 = assets.open("pdf_html_viewer_1.html").bufferedReader().use { it.readText() }
            val htmlPart2 = assets.open("pdf_html_viewer_2.html").bufferedReader().use { it.readText() }
            htmlFile.writeText(htmlPart1 + base64File.readText() + htmlPart2)

            base64File.delete()
            pdfFile.delete()

            withContext(Dispatchers.Main) {
                pdfOpenInWebView(htmlFile.absolutePath)
            }
        }
    }

    private fun writeResponseBodyToDisk(file: File, body: ResponseBody): Boolean {
        return try {
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

    private fun encodeFileToBase64(file: File): String {
        val bytes = file.readBytes()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(bytes)
        } else {
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        }
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
