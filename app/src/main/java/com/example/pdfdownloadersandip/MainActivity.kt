package com.example.pdfdownloadersandip

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.powerpdflibrary.PdfViewerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var pdfButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        pdfButton = findViewById(R.id.openPdfButton)

        pdfButton.setOnClickListener {
            val pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
            val pdfName = "dummy"
            PdfViewerActivity.openPdfViewer(this, pdfUrl, pdfName, "123dummy")
        }
    }
}