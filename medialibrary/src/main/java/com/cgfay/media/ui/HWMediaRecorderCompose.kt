package com.cgfay.media.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cgfay.media.recorder.HWMediaRecorder
import com.cgfay.media.recorder.OnRecordStateListener

/**
 * Compose helper used to remember [HWMediaRecorder] instance.
 */
@Composable
fun rememberHWMediaRecorder(listener: OnRecordStateListener): HWMediaRecorder =
    remember(listener) { HWMediaRecorder(listener) }
