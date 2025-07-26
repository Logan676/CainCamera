package com.cgfay.facedetect.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cgfay.facedetectlibrary.R
import com.cgfay.facedetect.listener.FaceTrackerCallback
import com.cgfay.facedetect.utils.ConUtil
import com.cgfay.facedetect.utils.FaceppConstraints
import com.cgfay.facedetect.utils.SensorEventUtil
import com.cgfay.landmark.LandmarkEngine
import com.cgfay.landmark.OneFace
import com.megvii.facepp.sdk.Facepp
import com.megvii.licensemanager.sdk.LicenseManager

/**
 * Face tracker implemented in Kotlin
 */
class FaceTracker private constructor() {

    private val mSyncFence = Any()
    private val mFaceTrackParam: FaceTrackParam = FaceTrackParam.getInstance()
    private var mTrackerThread: TrackerThread? = null

    fun setFaceCallback(callback: FaceTrackerCallback): FaceTrackerBuilder {
        return FaceTrackerBuilder(this, callback)
    }

    internal fun initTracker() {
        synchronized(mSyncFence) {
            mTrackerThread = TrackerThread("FaceTrackerThread").apply {
                start()
                waitUntilReady()
            }
        }
    }

    fun prepareFaceTracker(context: Context, orientation: Int, width: Int, height: Int) {
        synchronized(mSyncFence) {
            mTrackerThread?.prepareFaceTracker(context, orientation, width, height)
        }
    }

    fun trackFace(data: ByteArray, width: Int, height: Int) {
        synchronized(mSyncFence) {
            mTrackerThread?.trackFace(data, width, height)
        }
    }

    fun destroyTracker() {
        synchronized(mSyncFence) {
            mTrackerThread?.quitSafely()
        }
    }

    fun setBackCamera(backCamera: Boolean): FaceTracker {
        mFaceTrackParam.isBackCamera = backCamera
        return this
    }

    fun enable3DPose(enable: Boolean): FaceTracker {
        mFaceTrackParam.enable3DPose = enable
        return this
    }

    fun enableROIDetect(enable: Boolean): FaceTracker {
        mFaceTrackParam.enableROIDetect = enable
        return this
    }

    fun enable106Points(enable: Boolean): FaceTracker {
        mFaceTrackParam.enable106Points = enable
        return this
    }

    fun enableMultiFace(enable: Boolean): FaceTracker {
        mFaceTrackParam.enableMultiFace = enable
        return this
    }

    fun enableFaceProperty(enable: Boolean): FaceTracker {
        mFaceTrackParam.enableFaceProperty = enable
        return this
    }

    fun minFaceSize(size: Int): FaceTracker {
        mFaceTrackParam.minFaceSize = size
        return this
    }

    fun detectInterval(interval: Int): FaceTracker {
        mFaceTrackParam.detectInterval = interval
        return this
    }

    fun trackMode(mode: Int): FaceTracker {
        mFaceTrackParam.trackMode = mode
        return this
    }

    private class TrackerThread(name: String) : Thread(name) {
        private val mStartLock = Object()
        private var mReady = false

        private var facepp: Facepp? = null
        private var mSensorUtil: SensorEventUtil? = null

        private var mLooper: Looper? = null
        private var mHandler: Handler? = null

        override fun run() {
            Looper.prepare()
            synchronized(this) {
                mLooper = Looper.myLooper()
                (this as java.lang.Object).notifyAll()
                mHandler = Handler(mLooper!!)
            }
            synchronized(mStartLock) {
                mReady = true
                mStartLock.notify()
            }
            Looper.loop()
            synchronized(this) {
                release()
                mHandler?.removeCallbacksAndMessages(null)
                mHandler = null
            }
            synchronized(mStartLock) { mReady = false }
        }

        fun waitUntilReady() {
            synchronized(mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait()
                    } catch (_: InterruptedException) {
                    }
                }
            }
        }

        fun quitSafely(): Boolean {
            val looper = getLooper()
            return if (looper != null) {
                looper.quitSafely()
                true
            } else false
        }

        fun getLooper(): Looper? {
            if (!isAlive) return null
            synchronized(this) {
                while (isAlive && mLooper == null) {
                    try {
                        (this as java.lang.Object).wait()
                    } catch (_: InterruptedException) {
                    }
                }
            }
            return mLooper
        }

        fun prepareFaceTracker(context: Context, orientation: Int, width: Int, height: Int) {
            waitUntilReady()
            mHandler?.post { internalPrepareFaceTracker(context, orientation, width, height) }
        }

        fun trackFace(data: ByteArray, width: Int, height: Int) {
            waitUntilReady()
            mHandler?.post { internalTrackFace(data, width, height) }
        }

        private fun release() {
            ConUtil.releaseWakeLock()
            facepp?.release()
            facepp = null
        }

        @Synchronized
        private fun internalPrepareFaceTracker(context: Context, orientation: Int, width: Int, height: Int) {
            val faceTrackParam = FaceTrackParam.getInstance()
            if (!faceTrackParam.canFaceTrack) return
            release()
            facepp = Facepp()
            if (mSensorUtil == null) {
                mSensorUtil = SensorEventUtil(context)
            }
            ConUtil.acquireWakeLock(context)
            if (!faceTrackParam.previewTrack) {
                faceTrackParam.rotateAngle = orientation
            } else {
                faceTrackParam.rotateAngle = if (faceTrackParam.isBackCamera) orientation else 360 - orientation
            }
            var left = 0
            var top = 0
            var right = width
            var bottom = height
            if (faceTrackParam.enableROIDetect) {
                val line = height * faceTrackParam.roiRatio
                left = ((width - line) / 2.0f).toInt()
                top = ((height - line) / 2.0f).toInt()
                right = width - left
                bottom = height - top
            }
            facepp!!.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_5_2_model))
            val faceppConfig = facepp!!.faceppConfig
            faceppConfig.interval = faceTrackParam.detectInterval
            faceppConfig.minFaceSize = faceTrackParam.minFaceSize
            faceppConfig.roi_left = left
            faceppConfig.roi_top = top
            faceppConfig.roi_right = right
            faceppConfig.roi_bottom = bottom
            faceppConfig.one_face_tracking = if (faceTrackParam.enableMultiFace) 0 else 1
            faceppConfig.detectionMode = faceTrackParam.trackMode
            facepp!!.faceppConfig = faceppConfig
        }

        @Synchronized
        private fun internalTrackFace(data: ByteArray, widthInput: Int, heightInput: Int) {
            var width = widthInput
            var height = heightInput
            val faceTrackParam = FaceTrackParam.getInstance()
            if (!faceTrackParam.canFaceTrack || facepp == null) {
                LandmarkEngine.getInstance().setFaceSize(0)
                faceTrackParam.trackerCallback?.onTrackingFinish()
                return
            }
            val faceDetectTimeAction = System.currentTimeMillis()
            val orientation = if (faceTrackParam.previewTrack) mSensorUtil!!.orientation else 0
            var rotation = 0
            rotation = when (orientation) {
                0 -> faceTrackParam.rotateAngle
                1 -> 0
                2 -> 180
                3 -> 360 - faceTrackParam.rotateAngle
                else -> rotation
            }
            val faceppConfig = facepp!!.faceppConfig
            if (faceppConfig.rotation != rotation) {
                faceppConfig.rotation = rotation
                facepp!!.faceppConfig = faceppConfig
            }
            val faces = facepp!!.detect(data, width, height,
                if (faceTrackParam.previewTrack) Facepp.IMAGEMODE_NV21 else Facepp.IMAGEMODE_RGBA)
            if (VERBOSE) {
                val algorithmTime = System.currentTimeMillis() - faceDetectTimeAction
                Log.d("onFaceTracking", "track time = $algorithmTime")
            }
            LandmarkEngine.getInstance().setOrientation(orientation)
            val needFlip = faceTrackParam.previewTrack && !faceTrackParam.isBackCamera
            LandmarkEngine.getInstance().setNeedFlip(needFlip)
            if (faces != null && faces.isNotEmpty()) {
                for (index in faces.indices) {
                    if (faceTrackParam.enable106Points) {
                        facepp!!.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK106)
                    } else {
                        facepp!!.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK81)
                    }
                    if (faceTrackParam.enable3DPose) {
                        facepp!!.get3DPose(faces[index])
                    }
                    val face = faces[index]
                    val oneFace = LandmarkEngine.getInstance().getOneFace(index)
                    if (faceTrackParam.enableFaceProperty) {
                        facepp!!.getAgeGender(face)
                        oneFace.gender = if (face.female > face.male) OneFace.GENDER_WOMAN else OneFace.GENDER_MAN
                        oneFace.age = face.age.coerceAtLeast(1)
                    } else {
                        oneFace.gender = -1
                        oneFace.age = -1
                    }
                    oneFace.pitch = face.pitch
                    oneFace.yaw = if (faceTrackParam.isBackCamera) -face.yaw else face.yaw
                    oneFace.roll = face.roll
                    if (faceTrackParam.previewTrack) {
                        oneFace.roll = if (faceTrackParam.isBackCamera) {
                            (Math.PI / 2.0f + oneFace.roll).toFloat()
                        } else {
                            (Math.PI / 2.0f - face.roll).toFloat()
                        }
                    }
                    oneFace.confidence = face.confidence
                    if (faceTrackParam.previewTrack) {
                        if (orientation == 1 || orientation == 2) {
                            val temp = width
                            width = height
                            height = temp
                        }
                    }
                    if (oneFace.vertexPoints == null || oneFace.vertexPoints!!.size != face.points.size * 2) {
                        oneFace.vertexPoints = FloatArray(face.points.size * 2)
                    }
                    for (i in face.points.indices) {
                        var x = face.points[i].x / height * 2 - 1
                        var y = face.points[i].y / width * 2 - 1
                        val point = floatArrayOf(x, -y)
                        when (orientation) {
                            1 -> if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
                                point[0] = -y
                                point[1] = -x
                            } else {
                                point[0] = y
                                point[1] = x
                            }
                            2 -> if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
                                point[0] = y
                                point[1] = x
                            } else {
                                point[0] = -y
                                point[1] = -x
                            }
                            3 -> {
                                point[0] = -x
                                point[1] = y
                            }
                        }
                        if (faceTrackParam.previewTrack) {
                            if (faceTrackParam.isBackCamera) {
                                oneFace.vertexPoints!![2 * i] = point[0]
                            } else {
                                oneFace.vertexPoints!![2 * i] = -point[0]
                            }
                        } else {
                            oneFace.vertexPoints!![2 * i] = point[0]
                        }
                        oneFace.vertexPoints!![2 * i + 1] = point[1]
                    }
                    LandmarkEngine.getInstance().putOneFace(index, oneFace)
                }
            }
            LandmarkEngine.getInstance().setFaceSize(faces?.size ?: 0)
            faceTrackParam.trackerCallback?.onTrackingFinish()
        }
    }

    companion object {
        private const val TAG = "FaceTracker"
        private const val VERBOSE = false
        private val instance: FaceTracker by lazy { FaceTracker() }

        @JvmStatic
        fun getInstance(): FaceTracker = instance

        @JvmStatic
        fun requestFaceNetwork(context: Context) {
            if (Facepp.getSDKAuthType(ConUtil.getFileContent(context, R.raw.megviifacepp_0_5_2_model)) == 2) {
                FaceTrackParam.getInstance().canFaceTrack = true
                return
            }
            val licenseManager = LicenseManager(context)
            licenseManager.expirationMillis = Facepp.getApiExpirationMillis(context,
                ConUtil.getFileContent(context, R.raw.megviifacepp_0_5_2_model))
            val uuid = ConUtil.getUUIDString(context)
            val apiName = Facepp.getApiName()
            val url = FaceppConstraints.US_LICENSE_URL
            licenseManager.authTimeBufferMillis = 0
            licenseManager.takeLicenseFromNetwork(url, uuid, FaceppConstraints.API_KEY,
                FaceppConstraints.API_SECRET, apiName, "1",
                object : LicenseManager.TakeLicenseCallback {
                    override fun onSuccess() {
                        if (VERBOSE) Log.d(TAG, "success to register license!")
                        FaceTrackParam.getInstance().canFaceTrack = true
                    }

                    override fun onFailed(i: Int, bytes: ByteArray?) {
                        if (VERBOSE) Log.d(TAG, "Failed to register license!")
                        FaceTrackParam.getInstance().canFaceTrack = false
                    }
                })
        }
    }
}
