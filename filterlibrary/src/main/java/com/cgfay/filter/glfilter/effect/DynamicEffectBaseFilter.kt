package com.cgfay.filter.glfilter.effect

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.effect.bean.DynamicEffectData
import com.cgfay.filter.glfilter.resource.ResourceCodec
import com.cgfay.filter.glfilter.resource.ResourceDataCodec
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.uitls.utils.BitmapUtils
import java.io.IOException

/**
 * Base class of dynamic effect filter.
 */
open class DynamicEffectBaseFilter(
    context: Context,
    protected var mDynamicEffectData: DynamicEffectData?,
    unzipPath: String
) : GLImageFilter(
    context,
    if (mDynamicEffectData == null || TextUtils.isEmpty(mDynamicEffectData!!.vertexShader))
        VERTEX_SHADER else getShaderString(context, unzipPath, mDynamicEffectData!!.vertexShader),
    if (mDynamicEffectData == null || TextUtils.isEmpty(mDynamicEffectData!!.fragmentShader))
        FRAGMENT_SHADER else getShaderString(context, unzipPath, mDynamicEffectData!!.fragmentShader)
) {

    /** current timestamp in seconds */
    protected var mTimeStamp = 0f

    /** folder containing effect resources */
    protected var mFolderPath: String = if (unzipPath.startsWith("file://")) {
        unzipPath.substring("file://".length)
    } else {
        unzipPath
    }

    /** resource loader */
    protected var mResourceCodec: ResourceDataCodec? = null

    /** uniform value handles */
    protected val mUniformDataHandleList = HashMap<String, Int>()

    /** uniform sampler handles */
    protected val mUniformSamplerHandleList = HashMap<String, Int>()

    private var mTextureList: IntArray? = null
    private var mTextureWidthHandle = 0
    private var mTextureHeightHandle = 0

    init {
        initEffectUniformHandle()
    }

    /**
     * Bind uniform variables for the effect.
     */
    protected open fun initEffectUniformHandle() {
        mDynamicEffectData?.let { data ->
            val pair: Pair<String, String>? = ResourceCodec.getResourceFile(mFolderPath)
            if (pair != null) {
                mResourceCodec = ResourceDataCodec(
                    "$mFolderPath/${pair.first}",
                    "$mFolderPath/${pair.second}"
                )
            }
            if (mResourceCodec != null) {
                try {
                    mResourceCodec!!.init()
                } catch (e: IOException) {
                    Log.e(TAG, "initEffectUniformHandle: ", e)
                    mResourceCodec = null
                }
            }

            if (data.uniformSamplerList != null && data.uniformSamplerList.size > 0) {
                mTextureList = IntArray(data.uniformSamplerList.size)
                for (i in data.uniformSamplerList.indices) {
                    val sampler = data.uniformSamplerList[i]
                    if (sampler != null) {
                        val uniform = GLES30.glGetUniformLocation(mProgramHandle, sampler.uniform)
                        mUniformSamplerHandleList[sampler.uniform] = uniform
                        var bitmap: Bitmap? = null
                        if (mResourceCodec != null) {
                            bitmap = mResourceCodec!!.loadBitmap(sampler.value)
                        }
                        if (bitmap == null) {
                            bitmap = BitmapUtils.getBitmapFromFile("$mFolderPath/${sampler.value}")
                        }
                        if (bitmap != null) {
                            mTextureList!![i] = OpenGLUtils.createTexture(bitmap)
                            bitmap.recycle()
                        } else {
                            mTextureList!![i] = OpenGLUtils.GL_NOT_TEXTURE
                        }
                    }
                }
            }

            if (data.uniformDataList != null && data.uniformDataList.size > 0) {
                for (uniformData in data.uniformDataList) {
                    if (uniformData != null) {
                        val uniform = GLES30.glGetUniformLocation(mProgramHandle, uniformData.uniform)
                        mUniformDataHandleList[uniformData.uniform] = uniform
                    }
                }
            }

            if (data.texelSize) {
                mTextureWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "textureWidth")
                mTextureHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "textureHeight")
            } else {
                mTextureWidthHandle = OpenGLUtils.GL_NOT_INIT
                mTextureHeightHandle = OpenGLUtils.GL_NOT_INIT
            }
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        val data = mDynamicEffectData ?: return
        val frameIndex = (mTimeStamp / data.duration).toInt()
        for (uniformData in data.uniformDataList) {
            if (uniformData != null && uniformData.value != null) {
                if (frameIndex > uniformData.value.size) {
                    val currentIndex = frameIndex % uniformData.value.size
                    mUniformDataHandleList[uniformData.uniform]?.let {
                        GLES30.glUniform1f(it, uniformData.value[currentIndex])
                    }
                }
            }
        }
        if (mTextureList != null) {
            for ((i, sampler) in data.uniformSamplerList.withIndex()) {
                if (sampler != null && sampler.value != null) {
                    mUniformSamplerHandleList[sampler.uniform]?.let {
                        OpenGLUtils.bindTexture(it, mTextureList!![i], i + 1)
                    }
                }
            }
        }
        if (data.texelSize) {
            GLES30.glUniform1i(mTextureWidthHandle, mImageWidth)
            GLES30.glUniform1i(mTextureHeightHandle, mImageHeight)
        }
    }

    fun setTimeStamp(timeStamp: Float) {
        mTimeStamp = timeStamp
    }

    companion object {
        protected fun getShaderString(context: Context, unzipPath: String, shaderName: String): String {
            require(!(TextUtils.isEmpty(unzipPath) || TextUtils.isEmpty(shaderName))) { "shader is empty!" }
            val path = "$unzipPath/$shaderName"
            return when {
                path.startsWith("assets://") ->
                    OpenGLUtils.getShaderFromAssets(context, path.substring("assets://".length))
                path.startsWith("file://") ->
                    OpenGLUtils.getShaderFromFile(path.substring("file://".length))
                else -> OpenGLUtils.getShaderFromFile(path)
            }
        }
    }
}
