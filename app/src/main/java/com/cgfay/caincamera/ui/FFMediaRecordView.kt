package com.cgfay.caincamera.ui

import android.graphics.SurfaceTexture

interface FFMediaRecordView {
    fun hidViews()
    fun showViews()
    fun setProgress(progress: Float)
    fun addProgressSegment(progress: Float)
    fun deleteProgressSegment()
    fun bindSurfaceTexture(surfaceTexture: SurfaceTexture)
    fun updateTextureSize(width: Int, height: Int)
    fun onFrameAvailable()
    fun showProgressDialog()
    fun hideProgressDialog()
    fun showToast(msg: String)
}
