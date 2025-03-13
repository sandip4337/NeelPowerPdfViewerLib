package com.example.powerpdflibrary

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager

class PdfProgressDialog(context: Context, private val isDismissOnBack: Boolean = true) : AlertDialog(context) {

    companion object{
        private const val TAG = "BBProgressDialog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = $savedInstanceState")
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog,null,false)
        setContentView(view)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        setCanceledOnTouchOutside(false)
    }

    override fun onBackPressed() {
        if (isDismissOnBack)
            this.dismiss()
        return
    }

}