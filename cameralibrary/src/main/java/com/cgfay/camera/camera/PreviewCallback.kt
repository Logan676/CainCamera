package com.cgfay.camera.camera

/**
 * 预览回调数据
 */
fun interface PreviewCallback {
    fun onPreviewFrame(data: ByteArray)
}
