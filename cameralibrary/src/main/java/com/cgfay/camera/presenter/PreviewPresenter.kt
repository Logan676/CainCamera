package com.cgfay.camera.presenter

import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.media.recorder.SpeedMode

abstract class PreviewPresenter<T>(target: T) : IPresenter<T>(target) {

    abstract fun onBindSharedContext(context: EGLContext)
    abstract fun onRecordFrameAvailable(texture: Int, timestamp: Long)

    abstract fun onSurfaceCreated(surfaceTexture: SurfaceTexture)
    abstract fun onSurfaceChanged(width: Int, height: Int)
    abstract fun onSurfaceDestroyed()

    abstract fun changeResource(resourceData: ResourceData)
    abstract fun changeDynamicFilter(color: DynamicColor?)
    abstract fun changeDynamicMakeup(makeup: DynamicMakeup?)
    abstract fun changeDynamicFilter(filterIndex: Int)
    abstract fun previewFilter(): Int
    abstract fun nextFilter(): Int
    abstract fun getFilterIndex(): Int
    abstract fun showCompare(enable: Boolean)

    abstract fun takePicture()
    abstract fun switchCamera()
    abstract fun startRecord()
    abstract fun stopRecord()
    abstract fun cancelRecord()
    abstract fun isRecording(): Boolean
    abstract fun setRecordAudioEnable(enable: Boolean)
    abstract fun setRecordSeconds(seconds: Int)
    abstract fun setSpeedMode(mode: SpeedMode)
    abstract fun deleteLastVideo()
    abstract fun getRecordedVideoSize(): Int
    abstract fun changeFlashLight(on: Boolean)
    abstract fun enableEdgeBlurFilter(enable: Boolean)
    abstract fun setMusicPath(path: String?)
    abstract fun onOpenCameraSettingPage()
}
