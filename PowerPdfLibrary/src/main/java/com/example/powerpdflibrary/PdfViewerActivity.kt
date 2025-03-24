package com.example.powerpdflibrary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var fileNotPdf: TextView
    private var pdfUrl: String? = null
    private var pdfName: String? = null
    private var pdfId: String? = null
    private var baseUrl: String? = null
    private var isDownload: Boolean? = false
    private var progressDialog: PdfProgressDialog? = null
    private lateinit var filesDirPath: String
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("PowerPdfLibraryFilePrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        webView = findViewById(R.id.webView)
        fileNotPdf = findViewById(R.id.fileNotPdf)

        pdfName = intent.getStringExtra("PDF_NAME") ?: ""
        pdfUrl = intent.getStringExtra("PDF_URL") ?: ""
        pdfId = intent.getStringExtra("PDF_ID") ?: ""
        baseUrl = intent.getStringExtra("BASE_URL")?: "https://example.com/"


        if (pdfUrl?.isNotEmpty() == true && pdfName?.isNotEmpty() == true && pdfId?.isNotEmpty() == true) {
            filesDirPath = filesDir.absolutePath
            val base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
            val htmlFilePath = "$filesDirPath/$pdfName$pdfId.html"

            showProgressDialog(true)

            lifecycleScope.launch {
                if (File(base46FilePath).exists()) {
                    buildHtmlContent(File(base46FilePath), File(htmlFilePath))
                } else {
                    isDownload = true
                    downloadPdf()
                }
            }
        } else {
            fileNotPdf.text = "Sorry!!! Pdf information is missing"
            fileNotPdf.visibility = View.VISIBLE
        }
    }

    private fun downloadPdf() {
        val workManager = WorkManager.getInstance(this@PdfViewerActivity)

        val data = Data.Builder()
            .putString("PDF_ID", pdfId)
            .putString("PDF_URL", pdfUrl)
            .putString("PDF_NAME", pdfName)
            .putString("BASE_URL", baseUrl)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<PdfDownloadWorker>()
            .addTag(pdfId!!) // Tagging the worker
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(pdfId!!, ExistingWorkPolicy.KEEP, downloadRequest)

        observeWorkInfo(pdfId!!)
    }

    private fun observeWorkInfo(pdfId: String) {
        val workManager = WorkManager.getInstance(this)
        workManager.getWorkInfosByTagLiveData(pdfId).observe(this) { workInfos ->
            workInfos?.forEach { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt("PROGRESS", 0)
                        Log.e("PdfDownloadWorker", "Progress: $progress for PDF ${pdfId}")
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
                        val htmlFilePath = "$filesDirPath/$pdfName$pdfId.html"
                        saveFilePath(base46FilePath)
                        lifecycleScope.launch {
                            if (File(base46FilePath).exists()) {
                                buildHtmlContent(File(base46FilePath), File(htmlFilePath))
                            }
                        }
                    }
                    WorkInfo.State.FAILED -> {
                        Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    else -> {}
                }
            }
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
                withContext(Dispatchers.Main){
                    pdfOpenInWebView(outputHtmlFile.absolutePath)
                }
            } catch (e: IOException) {
                Log.e("PdfDownloadWorker", "HTML file creation failed: ${e.message}")
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

    private fun saveFilePath(filePath: String) {
        val filePaths = sharedPreferences.getStringSet("filePaths", mutableSetOf())?.toMutableSet()
        filePaths?.add(filePath)
        sharedPreferences.edit().putStringSet("filePaths", filePaths).apply()
    }

    private fun showProgressDialog(isDismissOnBack: Boolean) {
        if (progressDialog == null) {
            progressDialog = PdfProgressDialog(context = this, isDismissOnBack)
        }
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    companion object {
        fun openPdfViewer(context: Context, pdfUrl: String, pdfName: String, pdfId: String, baseUrl: String) {
            val intent = Intent(context, PdfViewerActivity::class.java).apply {
                putExtra("PDF_URL", pdfUrl)
                putExtra("PDF_NAME", pdfName)
                putExtra("PDF_ID", pdfId)
                putExtra("BASE_URL", baseUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val htmlFilePath = "$filesDirPath/$pdfName$pdfId.html"

        if (webView.canGoBack()) {
            val file = File(htmlFilePath)
            if (file.exists()) {
                file.delete()
            }
            webView.goBack()
        } else {
            val file = File(htmlFilePath)
            if (file.exists()) {
                file.delete()
            }
            super.onBackPressed()
        }
    }
}
