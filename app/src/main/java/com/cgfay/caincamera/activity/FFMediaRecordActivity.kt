package com.cgfay.caincamera.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.caincamera.ui.FFMediaRecordScreen

class FFMediaRecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FFMediaRecordScreen { finish() }
        }
    }
}
