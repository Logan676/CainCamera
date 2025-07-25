package com.cgfay.landmark

import android.util.SparseArray

/**
 * Engine for face landmark handling.
 */
object LandmarkEngine {

    private val mSyncFence = Any()
    // 人脸对象列表
    // 由于人脸数据个数有限，图像中的人脸个数小于千级，而且人脸索引是连续的，用SparseArray比Hashmap性能要更好
    private val mFaceArrays = SparseArray<OneFace>()

    // 手机当前的方向，0表示正屏幕，3表示倒过来，1表示左屏幕，2表示右屏幕
    private var mOrientation = 0f
    private var mNeedFlip = false

    @JvmStatic
    fun getInstance(): LandmarkEngine = this

    fun setOrientation(orientation: Int) {
        mOrientation = orientation.toFloat()
    }

    fun setNeedFlip(flip: Boolean) {
        mNeedFlip = flip
    }

    fun setFaceSize(size: Int) {
        synchronized(mSyncFence) {
            if (mFaceArrays.size() > size) {
                mFaceArrays.removeAtRange(size, mFaceArrays.size() - size)
            }
        }
    }

    fun hasFace(): Boolean = synchronized(mSyncFence) { mFaceArrays.size() > 0 }

    fun getOneFace(index: Int): OneFace {
        synchronized(mSyncFence) {
            return mFaceArrays[index] ?: OneFace().also { mFaceArrays.put(index, it) }
        }
    }

    fun putOneFace(index: Int, oneFace: OneFace) {
        synchronized(mSyncFence) { mFaceArrays.put(index, oneFace) }
    }

    fun getFaceSize(): Int = mFaceArrays.size()

    fun getFaceArrays(): SparseArray<OneFace> = mFaceArrays

    fun clearAll() {
        synchronized(mSyncFence) { mFaceArrays.clear() }
    }

    fun calculateExtraFacePoints(vertexPoints: FloatArray, index: Int) {
        if (index >= mFaceArrays.size() || mFaceArrays[index] == null ||
            mFaceArrays[index].vertexPoints == null ||
            mFaceArrays[index].vertexPoints!!.size + 8 * 2 > vertexPoints.size
        ) {
            return
        }
        val oneFace = mFaceArrays[index]
        System.arraycopy(oneFace.vertexPoints, 0, vertexPoints, 0, oneFace.vertexPoints!!.size)
        val point = FloatArray(2)
        // 嘴唇中心
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.mouthUpperLipBottom * 2],
            vertexPoints[FaceLandmark.mouthUpperLipBottom * 2 + 1],
            vertexPoints[FaceLandmark.mouthLowerLipTop * 2],
            vertexPoints[FaceLandmark.mouthLowerLipTop * 2 + 1]
        )
        vertexPoints[FaceLandmark.mouthCenter * 2] = point[0]
        vertexPoints[FaceLandmark.mouthCenter * 2 + 1] = point[1]

        // 左眉心
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.leftEyebrowUpperMiddle * 2],
            vertexPoints[FaceLandmark.leftEyebrowUpperMiddle * 2 + 1],
            vertexPoints[FaceLandmark.leftEyebrowLowerMiddle * 2],
            vertexPoints[FaceLandmark.leftEyebrowLowerMiddle * 2 + 1]
        )
        vertexPoints[FaceLandmark.leftEyebrowCenter * 2] = point[0]
        vertexPoints[FaceLandmark.leftEyebrowCenter * 2 + 1] = point[1]

        // 右眉心
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.rightEyebrowUpperMiddle * 2],
            vertexPoints[FaceLandmark.rightEyebrowUpperMiddle * 2 + 1],
            vertexPoints[FaceLandmark.rightEyebrowLowerMiddle * 2],
            vertexPoints[FaceLandmark.rightEyebrowLowerMiddle * 2 + 1]
        )
        vertexPoints[FaceLandmark.rightEyebrowCenter * 2] = point[0]
        vertexPoints[FaceLandmark.rightEyebrowCenter * 2 + 1] = point[1]

        // 额头中心
        vertexPoints[FaceLandmark.headCenter * 2] =
            vertexPoints[FaceLandmark.eyeCenter * 2] * 2f - vertexPoints[FaceLandmark.noseLowerMiddle * 2]
        vertexPoints[FaceLandmark.headCenter * 2 + 1] =
            vertexPoints[FaceLandmark.eyeCenter * 2 + 1] * 2f - vertexPoints[FaceLandmark.noseLowerMiddle * 2 + 1]

        // 额头左侧，备注：这个点不太准确，后续优化
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.leftEyebrowLeftTopCorner * 2],
            vertexPoints[FaceLandmark.leftEyebrowLeftTopCorner * 2 + 1],
            vertexPoints[FaceLandmark.headCenter * 2],
            vertexPoints[FaceLandmark.headCenter * 2 + 1]
        )
        vertexPoints[FaceLandmark.leftHead * 2] = point[0]
        vertexPoints[FaceLandmark.leftHead * 2 + 1] = point[1]

        // 额头右侧，备注：这个点不太准确，后续优化
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.rightEyebrowRightTopCorner * 2],
            vertexPoints[FaceLandmark.rightEyebrowRightTopCorner * 2 + 1],
            vertexPoints[FaceLandmark.headCenter * 2],
            vertexPoints[FaceLandmark.headCenter * 2 + 1]
        )
        vertexPoints[FaceLandmark.rightHead * 2] = point[0]
        vertexPoints[FaceLandmark.rightHead * 2 + 1] = point[1]

        // 左脸颊中心
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.leftCheekEdgeCenter * 2],
            vertexPoints[FaceLandmark.leftCheekEdgeCenter * 2 + 1],
            vertexPoints[FaceLandmark.noseLeft * 2],
            vertexPoints[FaceLandmark.noseLeft * 2 + 1]
        )
        vertexPoints[FaceLandmark.leftCheekCenter * 2] = point[0]
        vertexPoints[FaceLandmark.leftCheekCenter * 2 + 1] = point[1]

        // 右脸颊中心
        FacePointsUtils.getCenter(
            point,
            vertexPoints[FaceLandmark.rightCheekEdgeCenter * 2],
            vertexPoints[FaceLandmark.rightCheekEdgeCenter * 2 + 1],
            vertexPoints[FaceLandmark.noseRight * 2],
            vertexPoints[FaceLandmark.noseRight * 2 + 1]
        )
        vertexPoints[FaceLandmark.rightCheekCenter * 2] = point[0]
        vertexPoints[FaceLandmark.rightCheekCenter * 2 + 1] = point[1]
    }

    private fun calculateImageEdgePoints(vertexPoints: FloatArray) {
        if (vertexPoints.size < 122 * 2) {
            return
        }

        when (mOrientation.toInt()) {
            0 -> {
                vertexPoints[114 * 2] = 0f
                vertexPoints[114 * 2 + 1] = 1f
                vertexPoints[115 * 2] = 1f
                vertexPoints[115 * 2 + 1] = 1f
                vertexPoints[116 * 2] = 1f
                vertexPoints[116 * 2 + 1] = 0f
                vertexPoints[117 * 2] = 1f
                vertexPoints[117 * 2 + 1] = -1f
            }
            1 -> {
                vertexPoints[114 * 2] = 1f
                vertexPoints[114 * 2 + 1] = 0f
                vertexPoints[115 * 2] = 1f
                vertexPoints[115 * 2 + 1] = -1f
                vertexPoints[116 * 2] = 0f
                vertexPoints[116 * 2 + 1] = -1f
                vertexPoints[117 * 2] = -1f
                vertexPoints[117 * 2 + 1] = -1f
            }
            2 -> {
                vertexPoints[114 * 2] = -1f
                vertexPoints[114 * 2 + 1] = 0f
                vertexPoints[115 * 2] = -1f
                vertexPoints[115 * 2 + 1] = 1f
                vertexPoints[116 * 2] = 0f
                vertexPoints[116 * 2 + 1] = 1f
                vertexPoints[117 * 2] = 1f
                vertexPoints[117 * 2 + 1] = 1f
            }
            3 -> {
                vertexPoints[114 * 2] = 0f
                vertexPoints[114 * 2 + 1] = -1f
                vertexPoints[115 * 2] = -1f
                vertexPoints[115 * 2 + 1] = -1f
                vertexPoints[116 * 2] = -1f
                vertexPoints[116 * 2 + 1] = 0f
                vertexPoints[117 * 2] = -1f
                vertexPoints[117 * 2 + 1] = 1f
            }
        }
        // 118 ~ 121 与 114 ~ 117 的顶点坐标恰好反过来
        vertexPoints[118 * 2] = -vertexPoints[114 * 2]
        vertexPoints[118 * 2 + 1] = -vertexPoints[114 * 2 + 1]
        vertexPoints[119 * 2] = -vertexPoints[115 * 2]
        vertexPoints[119 * 2 + 1] = -vertexPoints[115 * 2 + 1]
        vertexPoints[120 * 2] = -vertexPoints[116 * 2]
        vertexPoints[120 * 2 + 1] = -vertexPoints[116 * 2 + 1]
        vertexPoints[121 * 2] = -vertexPoints[117 * 2]
        vertexPoints[121 * 2 + 1] = -vertexPoints[117 * 2 + 1]

        if (mNeedFlip) {
            for (i in 0 until 8) {
                vertexPoints[(114 + i) * 2] = -vertexPoints[(114 + i) * 2]
                vertexPoints[(114 + i) * 2 + 1] = -vertexPoints[(114 + i) * 2 + 1]
            }
        }
    }

    fun updateFaceAdjustPoints(vertexPoints: FloatArray, texturePoints: FloatArray, faceIndex: Int) {
        if (vertexPoints.size != 122 * 2 || texturePoints.size != 122 * 2) {
            return
        }
        calculateExtraFacePoints(vertexPoints, faceIndex)
        calculateImageEdgePoints(vertexPoints)
        for (i in vertexPoints.indices) {
            texturePoints[i] = vertexPoints[i] * 0.5f + 0.5f
        }
    }

    fun getShadowVertices(vetexPoints: FloatArray, faceIndex: Int) {
        // TODO: not implemented
    }

    fun getBlushVertices(vertexPoints: FloatArray, faceIndex: Int) {
        // TODO: not implemented
    }

    fun getEyeBrowVertices(vertexPoints: FloatArray, faceIndex: Int) {
        // TODO: not implemented
    }

    @Synchronized
    fun getEyeVertices(vertexPoints: FloatArray, faceIndex: Int) {
        if (vertexPoints.size < 80 || faceIndex >= mFaceArrays.size() || mFaceArrays[faceIndex] == null) {
            return
        }

        for (i in 0 until 4) {
            vertexPoints[i * 2] = mFaceArrays[faceIndex].vertexPoints!![i * 2]
            vertexPoints[i * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![i * 2 + 1]
        }

        for (i in 29 until 34) {
            vertexPoints[(i - 29 + 4) * 2] = mFaceArrays[faceIndex].vertexPoints!![i * 2]
            vertexPoints[(i - 29 + 4) * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![i * 2 + 1]
        }

        for (i in 42 until 45) {
            vertexPoints[(i - 42 + 9) * 2] = mFaceArrays[faceIndex].vertexPoints!![i * 2]
            vertexPoints[(i - 42 + 9) * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![i * 2 + 1]
        }

        for (i in 52 until 74) {
            vertexPoints[(i - 52 + 12) * 2] = mFaceArrays[faceIndex].vertexPoints!![i * 2]
            vertexPoints[(i - 52 + 12) * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![i * 2 + 1]
        }

        vertexPoints[34 * 2] = mFaceArrays[faceIndex].vertexPoints!![75 * 2]
        vertexPoints[34 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![75 * 2 + 1]

        vertexPoints[35 * 2] = mFaceArrays[faceIndex].vertexPoints!![76 * 2]
        vertexPoints[35 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![76 * 2 + 1]

        vertexPoints[36 * 2] = mFaceArrays[faceIndex].vertexPoints!![78 * 2]
        vertexPoints[36 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![78 * 2 + 1]

        vertexPoints[37 * 2] = mFaceArrays[faceIndex].vertexPoints!![79 * 2]
        vertexPoints[37 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![79 * 2 + 1]

        vertexPoints[38 * 2] =
            (mFaceArrays[faceIndex].vertexPoints!![3 * 2] + mFaceArrays[faceIndex].vertexPoints!![44 * 2]) * 0.5f
        vertexPoints[38 * 2 + 1] =
            (mFaceArrays[faceIndex].vertexPoints!![3 * 2 + 1] + mFaceArrays[faceIndex].vertexPoints!![44 * 2 + 1]) * 0.5f

        vertexPoints[39 * 2] =
            (mFaceArrays[faceIndex].vertexPoints!![29 * 2] + mFaceArrays[faceIndex].vertexPoints!![44 * 2]) * 0.5f
        vertexPoints[39 * 2 + 1] =
            (mFaceArrays[faceIndex].vertexPoints!![29 * 2 + 1] + mFaceArrays[faceIndex].vertexPoints!![44 * 2 + 1]) * 0.5f
    }

    @Synchronized
    fun getLipsVertices(vertexPoints: FloatArray, faceIndex: Int) {
        if (vertexPoints.size < 40 || faceIndex >= mFaceArrays.size() || mFaceArrays[faceIndex] == null) {
            return
        }
        for (i in 0 until 20) {
            vertexPoints[i * 2] = mFaceArrays[faceIndex].vertexPoints!![(84 + i) * 2]
            vertexPoints[i * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![(84 + i) * 2 + 1]
        }
    }

    @Synchronized
    fun getBrightEyeVertices(vertexPoints: FloatArray, faceIndex: Int) {
        if (vertexPoints.size < 32 || faceIndex >= mFaceArrays.size() || mFaceArrays[faceIndex] == null) {
            return
        }
        for (i in 52 until 64) {
            vertexPoints[(i - 52) * 2] = mFaceArrays[faceIndex].vertexPoints!![i * 2]
            vertexPoints[(i - 52) * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![i * 2 + 1]
        }

        vertexPoints[12 * 2] = mFaceArrays[faceIndex].vertexPoints!![72 * 2]
        vertexPoints[12 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![72 * 2 + 1]

        vertexPoints[13 * 2] = mFaceArrays[faceIndex].vertexPoints!![73 * 2]
        vertexPoints[13 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![73 * 2 + 1]

        vertexPoints[14 * 2] = mFaceArrays[faceIndex].vertexPoints!![75 * 2]
        vertexPoints[14 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![75 * 2 + 1]

        vertexPoints[15 * 2] = mFaceArrays[faceIndex].vertexPoints!![76 * 2]
        vertexPoints[15 * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![76 * 2 + 1]
    }

    @Synchronized
    fun getBeautyTeethVertices(vertexPoints: FloatArray, faceIndex: Int) {
        if (vertexPoints.size < 24 || faceIndex >= mFaceArrays.size() || mFaceArrays[faceIndex] == null) {
            return
        }
        for (i in 84 until 96) {
            vertexPoints[(i - 84) * 2] = mFaceArrays[faceIndex].vertexPoints!![i * 2]
            vertexPoints[(i - 84) * 2 + 1] = mFaceArrays[faceIndex].vertexPoints!![i * 2 + 1]
        }
    }
}
