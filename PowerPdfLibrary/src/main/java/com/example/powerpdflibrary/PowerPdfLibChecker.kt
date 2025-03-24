package com.example.powerpdflibrary

import android.content.Context
import java.io.File

class PowerPdfLibChecker {

    companion object {

        private lateinit var filesDirPath: String

        fun checkPdfExits(context: Context, pdfName: String, pdfId: String): Boolean {
            filesDirPath = context.filesDir.absolutePath
            val base46FilePath = "$filesDirPath/base64_$pdfName$pdfId.txt"
            return File(base46FilePath).exists()
        }
    }
}