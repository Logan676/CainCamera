package com.cgfay.filter.glfilter.stickers

import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerNormalData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import com.cgfay.landmark.FacePointsUtils
import com.cgfay.landmark.LandmarkEngine
import com.cgfay.landmark.OneFace
import java.nio.FloatBuffer
import kotlin.math.abs

/**
 * 绘制普通贴纸(非前景贴纸)
 */
class DynamicStickerNormalFilter(context: Context, sticker: DynamicSticker?) :
    DynamicStickerBaseFilter(
        context,
        sticker,
        OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_normal.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_normal.glsl")
    ) {

    private var mMVPMatrixHandle = 0

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    private var mRatio = 0f

    private var mVertexBuffer: FloatBuffer? = null
    private var mTextureBuffer: FloatBuffer? = null

    private val mStickerVertices = FloatArray(8)

    init {
        if (mDynamicSticker != null && mDynamicSticker!!.dataList != null) {
            for (i in mDynamicSticker!!.dataList.indices) {
                if (mDynamicSticker!!.dataList[i] is DynamicStickerNormalData) {
                    val path = mDynamicSticker!!.unzipPath + "/" + mDynamicSticker!!.dataList[i].stickerName
                    mStickerLoaderList.add(DynamicStickerLoader(this, mDynamicSticker!!.dataList[i], path))
                }
            }
        }
        initMatrix()
        initBuffer()
    }

    private fun initMatrix() {
        Matrix.setIdentityM(mProjectionMatrix, 0)
        Matrix.setIdentityM(mViewMatrix, 0)
        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
    }

    private fun initBuffer() {
        releaseBuffer()
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices_flipx)
    }

    private fun releaseBuffer() {
        mVertexBuffer?.clear()
        mVertexBuffer = null
        mTextureBuffer?.clear()
        mTextureBuffer = null
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        mMVPMatrixHandle = if (mProgramHandle != OpenGLUtils.GL_NOT_INIT)
            GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        else OpenGLUtils.GL_NOT_INIT
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        mRatio = width.toFloat() / height
        Matrix.frustumM(mProjectionMatrix, 0, -mRatio, mRatio, -1.0f, 1.0f, 3.0f, 9.0f)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 6.0f, 0f, 0f, 0f, 0f, 1.0f, 0f)
    }

    override fun release() {
        super.release()
        releaseBuffer()
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        val stickerTexture = drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        return super.drawFrame(stickerTexture, vertexBuffer, textureBuffer)
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        Matrix.setIdentityM(mMVPMatrix, 0)
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        if (mStickerLoaderList.isNotEmpty() && LandmarkEngine.getInstance().hasFace()) {
            val faceCount = Math.min(LandmarkEngine.getInstance().faceSize, mStickerLoaderList[0].getMaxCount())
            for (faceIndex in 0 until faceCount) {
                val oneFace = LandmarkEngine.getInstance().getOneFace(faceIndex)
                if (oneFace.confidence > 0.5f) {
                    for (stickerIndex in mStickerLoaderList.indices) {
                        synchronized(this) {
                            mStickerLoaderList[stickerIndex].updateStickerTexture()
                            calculateStickerVertices(
                                mStickerLoaderList[stickerIndex].getStickerData() as DynamicStickerNormalData,
                                oneFace
                            )
                            super.drawFrameBuffer(
                                mStickerLoaderList[stickerIndex].getStickerTexture(),
                                mVertexBuffer!!,
                                mTextureBuffer!!
                            )
                        }
                    }
                }
            }
            GLES30.glFlush()
        }
        return mFrameBufferTextures[0]
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        }
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE, GLES30.GL_ONE)
    }

    override fun onDrawFrameAfter() {
        super.onDrawFrameAfter()
        GLES30.glDisable(GLES30.GL_BLEND)
    }

    private fun calculateStickerVertices(stickerData: DynamicStickerNormalData, oneFace: OneFace?) {
        if (oneFace == null || oneFace.vertexPoints == null) {
            return
        }
        val stickerWidth = FacePointsUtils.getDistance(
            (oneFace.vertexPoints[stickerData.startIndex * 2] * 0.5f + 0.5f) * mImageWidth,
            (oneFace.vertexPoints[stickerData.startIndex * 2 + 1] * 0.5f + 0.5f) * mImageHeight,
            (oneFace.vertexPoints[stickerData.endIndex * 2] * 0.5f + 0.5f) * mImageWidth,
            (oneFace.vertexPoints[stickerData.endIndex * 2 + 1] * 0.5f + 0.5f) * mImageHeight
        ).toFloat() * stickerData.baseScale
        val stickerHeight = stickerWidth * stickerData.height.toFloat() / stickerData.width.toFloat()

        var centerX = 0f
        var centerY = 0f
        for (i in stickerData.centerIndexList!!.indices) {
            centerX += (oneFace.vertexPoints[stickerData.centerIndexList!![i] * 2] * 0.5f + 0.5f) * mImageWidth
            centerY += (oneFace.vertexPoints[stickerData.centerIndexList!![i] * 2 + 1] * 0.5f + 0.5f) * mImageHeight
        }
        centerX /= stickerData.centerIndexList!!.size.toFloat()
        centerY /= stickerData.centerIndexList!!.size.toFloat()
        centerX = centerX / mImageHeight * ProjectionScale
        centerY = centerY / mImageHeight * ProjectionScale
        val ndcCenterX = (centerX - mRatio) * ProjectionScale
        val ndcCenterY = (centerY - 1.0f) * ProjectionScale
        val ndcStickerWidth = stickerWidth / mImageHeight * ProjectionScale
        val ndcStickerHeight = ndcStickerWidth * stickerData.height.toFloat() / stickerData.width.toFloat()
        val offsetX = stickerWidth * stickerData.offsetX / mImageHeight * ProjectionScale
        val offsetY = stickerHeight * stickerData.offsetY / mImageHeight * ProjectionScale
        val anchorX = ndcCenterX + offsetX * ProjectionScale
        val anchorY = ndcCenterY + offsetY * ProjectionScale
        mStickerVertices[0] = anchorX - ndcStickerWidth
        mStickerVertices[1] = anchorY - ndcStickerHeight
        mStickerVertices[2] = anchorX + ndcStickerWidth
        mStickerVertices[3] = anchorY - ndcStickerHeight
        mStickerVertices[4] = anchorX - ndcStickerWidth
        mStickerVertices[5] = anchorY + ndcStickerHeight
        mStickerVertices[6] = anchorX + ndcStickerWidth
        mStickerVertices[7] = anchorY + ndcStickerHeight
        mVertexBuffer!!.clear()
        mVertexBuffer!!.position(0)
        mVertexBuffer!!.put(mStickerVertices)

        Matrix.setIdentityM(mModelMatrix, 0)
        Matrix.translateM(mModelMatrix, 0, ndcCenterX, ndcCenterY, 0f)
        var pitchAngle = (-oneFace.pitch * 180f / Math.PI).toFloat()
        var yawAngle = (oneFace.yaw * 180f / Math.PI).toFloat()
        var rollAngle = (oneFace.roll * 180f / Math.PI).toFloat()
        if (abs(yawAngle) > 50) yawAngle = (yawAngle / abs(yawAngle)) * 50
        if (abs(pitchAngle) > 30) pitchAngle = (pitchAngle / abs(pitchAngle)) * 30
        Matrix.rotateM(mModelMatrix, 0, rollAngle, 0f, 0f, 1f)
        Matrix.rotateM(mModelMatrix, 0, yawAngle, 0f, 1f, 0f)
        Matrix.rotateM(mModelMatrix, 0, pitchAngle, 1f, 0f, 0f)
        Matrix.translateM(mModelMatrix, 0, -ndcCenterX, -ndcCenterY, 0f)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mModelMatrix, 0)
    }

    companion object {
        private const val ProjectionScale = 2.0f
    }
}
