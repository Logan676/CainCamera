package com.cgfay.facedetect.engine

import com.cgfay.facedetect.listener.FaceTrackerCallback
import com.megvii.facepp.sdk.Facepp

/**
 * 人脸检测参数
 */
class FaceTrackParam private constructor() {
    // 是否允许检测
    var canFaceTrack: Boolean = false
    // 旋转角度
    var rotateAngle: Int = 0
    // 是否相机预览检测，true为预览检测，false为静态图片检测
    var previewTrack: Boolean = true
    // 是否允许3D姿态角
    var enable3DPose: Boolean = false
    // 是否允许区域检测
    var enableROIDetect: Boolean = false
    // 检测区域缩放比例
    var roiRatio: Float = 0.8f
    // 是否允许106个关键点
    var enable106Points: Boolean = true
    // 是否后置摄像头
    var isBackCamera: Boolean = false
    // 是否允许人脸年龄检测
    var enableFaceProperty: Boolean = false
    // 是否允许多人脸检测
    var enableMultiFace: Boolean = true
    // 最小人脸大小
    var minFaceSize: Int = 200
    // 检测间隔
    var detectInterval: Int = 25
    // 检测模式
    var trackMode: Int = Facepp.FaceppConfig.DETECTION_MODE_TRACKING
    // 检测回调
    var trackerCallback: FaceTrackerCallback? = null

    fun reset() {
        previewTrack = true
        enable3DPose = false
        enableROIDetect = false
        roiRatio = 0.8f
        enable106Points = true
        isBackCamera = false
        enableFaceProperty = false
        enableMultiFace = true
        minFaceSize = 200
        detectInterval = 25
        trackMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING
        trackerCallback = null
    }

    companion object {
        val instance: FaceTrackParam by lazy { FaceTrackParam() }
        fun getInstance(): FaceTrackParam = instance
    }
}
