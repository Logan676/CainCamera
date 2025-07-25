package com.cgfay.filter.glfilter.makeup

import android.opengl.GLES30
import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.landmark.LandmarkEngine
import java.nio.FloatBuffer

/**
 * Loader handling pupil (contact lens) makeup.
 */
class MakeupPupilLoader(
    filter: GLImageMakeupFilter,
    makeupData: MakeupBaseData?,
    folderPath: String
) : MakeupBaseLoader(filter, makeupData, folderPath) {

    private var mFrameBuffer: IntArray? = null
    private var mFrameBufferTexture: IntArray? = null

    override fun initBuffers() {
        // TODO create eye mask buffers if needed
    }

    override fun updateVertices(faceIndex: Int) {
        if (mVertexBuffer == null || mVertices == null) return
        mVertexBuffer!!.clear()
        if (LandmarkEngine.getInstance().hasFace() && LandmarkEngine.getInstance().faceSize > faceIndex) {
            LandmarkEngine.getInstance().getEyeVertices(mVertices, faceIndex)
        }
        mVertexBuffer!!.put(mVertices)
        mVertexBuffer!!.position(0)
    }

    override fun drawMakeup(faceIndex: Int, inputTexture: Int, vertexBuffer: FloatBuffer?, textureBuffer: FloatBuffer?) {
        if (mFrameBuffer == null || mFrameBufferTexture == null) return
        updateVertices(faceIndex)
        mWeakFilter.get()?.drawMakeup(mFrameBuffer!![0], inputTexture, mMaterialTexture, mMaskTexture,
            mVertexBuffer, mTextureBuffer, mIndexBuffer, mMakeupType, mStrength)
        mWeakFilter.get()?.drawMakeup(inputTexture, mFrameBufferTexture!![0], mMaskTexture,
            mVertexBuffer, mTextureBuffer, mIndexBuffer, mMakeupType, mStrength)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        initFrameBuffer(width, height)
    }

    private fun initFrameBuffer(width: Int, height: Int) {
        if (mFrameBuffer != null && (mImageWidth != width || mImageHeight != height)) {
            destroyFrameBuffer()
        }
        if (mFrameBuffer == null) {
            mFrameBuffer = IntArray(1)
            mFrameBufferTexture = IntArray(1)
            OpenGLUtils.createFrameBuffer(mFrameBuffer, mFrameBufferTexture, width, height)
        }
    }

    private fun destroyFrameBuffer() {
        if (mFrameBufferTexture != null) {
            GLES30.glDeleteTextures(1, mFrameBufferTexture, 0)
            mFrameBufferTexture = null
        }
        if (mFrameBuffer != null) {
            GLES30.glDeleteFramebuffers(1, mFrameBuffer, 0)
            mFrameBuffer = null
        }
    }

    override fun release() {
        super.release()
        destroyFrameBuffer()
    }
}
