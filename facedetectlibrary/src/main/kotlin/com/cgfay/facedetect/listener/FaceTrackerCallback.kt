package com.cgfay.facedetect.listener

/**
 * 人脸关键点检测回调
 * Created by cain.huang on 2017/11/10.
 */
fun interface FaceTrackerCallback {
    /**
     * 检测完成回调
     */
    fun onTrackingFinish()
}
