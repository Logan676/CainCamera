package com.cgfay.filter.glfilter.makeup

import android.content.Context
import android.opengl.GLES30
import android.util.SparseArray
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData
import com.cgfay.filter.glfilter.makeup.bean.MakeupType
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.landmark.LandmarkEngine
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Filter applying makeup effects.
 */
class GLImageMakeupFilter(
    context: Context,
    dynamicMakeup: DynamicMakeup? = null
) : GLImageFilter(
    context,
    OpenGLUtils.getShaderFromAssets(context, "shader/makeup/vertex_makeup.glsl"),
    OpenGLUtils.getShaderFromAssets(context, "shader/makeup/fragment_makeup.glsl")
) {

    private var maskTextureHandle = 0
    private var materialTextureHandle = 0
    private var strengthHandle = 0
    private var makeupTypeHandle = 0

    private var loaderArrays = SparseArray<MakeupBaseLoader>()

    init {
        // initialize empty loaders
        for (i in 0 until MakeupType.MakeupIndex.MakeupSize) {
            loaderArrays.put(i, null)
        }
        dynamicMakeup?.makeupList?.forEach { data ->
            data?.let {
                val loader = if (it.makeupType.typeName == "pupil") {
                    MakeupPupilLoader(this, it, dynamicMakeup.unzipPath)
                } else {
                    MakeupNormalLoader(this, it, dynamicMakeup.unzipPath)
                }
                loader.init(context)
                loaderArrays.put(it.makeupType.index, loader)
            }
        }
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            maskTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "maskTexture")
            materialTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "materialTexture")
            strengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength")
            makeupTypeHandle = GLES30.glGetUniformLocation(mProgramHandle, "makeupType")
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        GLES30.glUniform1i(makeupTypeHandle, 0)
    }

    override fun drawFrameBuffer(
        textureId: Int,
        vertexBuffer: FloatBuffer?,
        textureBuffer: FloatBuffer?
    ): Int {
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        if (LandmarkEngine.getInstance().hasFace()) {
            for (faceIndex in 0 until LandmarkEngine.getInstance().faceSize) {
                for (i in 0 until loaderArrays.size()) {
                    loaderArrays[i]?.drawMakeup(faceIndex, textureId, vertexBuffer, textureBuffer)
                }
            }
        }
        return mFrameBufferTextures[0]
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        for (i in 0 until loaderArrays.size()) {
            loaderArrays[i]?.onInputSizeChanged(width, height)
        }
    }

    override fun release() {
        super.release()
        for (i in 0 until loaderArrays.size()) {
            loaderArrays[i]?.release()
        }
        loaderArrays.clear()
    }

    fun changeMakeupData(makeupData: MakeupBaseData?, folderPath: String) {
        val data = makeupData ?: return
        val index = data.makeupType.index
        loaderArrays[index]?.let {
            it.changeMakeupData(data, folderPath)
            it.init(mContext)
        } ?: run {
            val loader = if (data.makeupType.typeName == "pupil") {
                MakeupPupilLoader(this, data, folderPath)
            } else {
                MakeupNormalLoader(this, data, folderPath)
            }
            loader.init(mContext)
            loader.onInputSizeChanged(mImageWidth, mImageHeight)
            loaderArrays.put(index, loader)
        }
    }

    fun changeMakeupData(dynamicMakeup: DynamicMakeup?) {
        for (i in 0 until loaderArrays.size()) {
            loaderArrays[i]?.reset()
        }
        dynamicMakeup?.makeupList?.forEach { data ->
            data?.let {
                val index = it.makeupType.index
                loaderArrays[index]?.let { loader ->
                    loader.changeMakeupData(it, dynamicMakeup.unzipPath)
                    loader.init(mContext)
                } ?: run {
                    val loader = if (it.makeupType.typeName == "pupil") {
                        MakeupPupilLoader(this, it, dynamicMakeup.unzipPath)
                    } else {
                        MakeupNormalLoader(this, it, dynamicMakeup.unzipPath)
                    }
                    loader.init(mContext)
                    loader.onInputSizeChanged(mImageWidth, mImageHeight)
                    loaderArrays.put(index, loader)
                }
            }
        }
    }

    fun drawMakeup(
        inputTexture: Int,
        materialTexture: Int,
        maskTexture: Int,
        vertexBuffer: FloatBuffer?,
        textureBuffer: FloatBuffer?,
        indexBuffer: ShortBuffer?,
        makeupType: Int,
        strength: Float
    ) {
        drawMakeup(
            mFrameBuffers[0],
            inputTexture,
            materialTexture,
            maskTexture,
            vertexBuffer,
            textureBuffer,
            indexBuffer,
            makeupType,
            strength
        )
    }

    fun drawMakeup(
        frameBuffer: Int,
        inputTexture: Int,
        materialTexture: Int,
        maskTexture: Int,
        vertexBuffer: FloatBuffer?,
        textureBuffer: FloatBuffer?,
        indexBuffer: ShortBuffer?,
        makeupType: Int,
        strength: Float
    ) {
        if (inputTexture == OpenGLUtils.GL_NOT_TEXTURE || indexBuffer == null) {
            return
        }

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer)
        GLES30.glUseProgram(mProgramHandle)
        runPendingOnDrawTasks()
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_COLOR)

        vertexBuffer?.let {
            it.position(0)
            GLES30.glVertexAttribPointer(mPositionHandle, 2, GLES30.GL_FLOAT, false, 0, it)
            GLES30.glEnableVertexAttribArray(mPositionHandle)
        }
        textureBuffer?.let {
            it.position(0)
            GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES30.GL_FLOAT, false, 0, it)
            GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle)
        }

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(getTextureType(), inputTexture)
        GLES30.glUniform1i(mInputTextureHandle, 0)

        if (materialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(getTextureType(), materialTexture)
            GLES30.glUniform1i(materialTextureHandle, 1)
        }

        if (maskTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
            GLES30.glBindTexture(getTextureType(), maskTexture)
            GLES30.glUniform1i(maskTextureHandle, 2)
        }
        GLES30.glUniform1i(makeupTypeHandle, makeupType)
        GLES30.glUniform1f(strengthHandle, strength)

        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            indexBuffer.capacity(),
            GLES30.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES30.glDisableVertexAttribArray(mPositionHandle)
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle)
        GLES30.glBindTexture(getTextureType(), 0)
        GLES30.glDisable(GLES30.GL_BLEND)

        GLES30.glUseProgram(0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }
}
