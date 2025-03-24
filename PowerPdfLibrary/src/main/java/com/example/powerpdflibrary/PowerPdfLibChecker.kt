package com.example.powerpdflibrary

import android.content.Context
import java.io.File

class PowerPdfLibChecker {

    companion object {

        private lateinit var filesDirPath: String
        private lateinit var base46FilePath: String

        fun checkPdfExits(context: Context, pdfName: String, pdfId: String): Boolean {
            filesDirPath = context.filesDir.absolutePath
            val base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
            return File(base46FilePath).exists()
        }

        fun getFilesDirPathAsFile(context: Context, pdfName: String, pdfId: String): File {
            filesDirPath = context.filesDir.absolutePath
            if (File("$filesDirPath/base64_$pdfName$pdfId.txt").exists()) {
                base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
            }
            return File(base46FilePath)
        }

        fun getFilesDirPathAsString(context: Context, pdfName: String, pdfId: String): String {
            filesDirPath = context.filesDir.absolutePath
            if (File("$filesDirPath/base64_$pdfName$pdfId.txt").exists()) {
                base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
            }
            return base46FilePath
        }

        fun deletePdfFile(context: Context, pdfName: String, pdfId: String) {
            filesDirPath = context.filesDir.absolutePath
            val base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
            val file = File(base46FilePath)
            if (file.exists()) {
                file.delete()
            } else {
                println("File not found: $base46FilePath")
            }
        }
    }
}