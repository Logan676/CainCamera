package com.cgfay.filter.glfilter.color

import android.graphics.Bitmap
import android.net.Uri
import android.opengl.GLES30
import android.text.TextUtils
import android.util.Log
import com.cgfay.filter.glfilter.color.bean.DynamicColorData
import com.cgfay.filter.glfilter.resource.ResourceCodec
import com.cgfay.filter.glfilter.resource.ResourceDataCodec
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.uitls.utils.BitmapUtils
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * 滤镜资源加载器
 */
class DynamicColorLoader(
    filter: DynamicColorBaseFilter,
    private var mColorData: DynamicColorData?,
    folderPath: String
) {

    companion object {
        private const val TAG = "DynamicColorLoader"
    }

    // 滤镜所在的文件夹
    private var mFolderPath: String = if (folderPath.startsWith("file://")) {
        folderPath.substring("file://".length)
    } else folderPath

    // 资源加载器
    private var mResourceCodec: ResourceDataCodec? = null
    // 动态滤镜
    private val mWeakFilter = WeakReference(filter)
    // 统一变量列表
    private val mUniformHandleList = HashMap<String, Int>()
    // 纹理列表
    private var mTextureList: IntArray? = null

    // 句柄
    private var mTexelWidthOffsetHandle = OpenGLUtils.GL_NOT_INIT
    private var mTexelHeightOffsetHandle = OpenGLUtils.GL_NOT_INIT
    private var mStrengthHandle = OpenGLUtils.GL_NOT_INIT
    private var mStrength = mColorData?.strength ?: 1.0f
    private var mTexelWidthOffset = 1.0f
    private var mTexelHeightOffset = 1.0f

    init {
        val pair = ResourceCodec.getResourceFile(mFolderPath)
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
                Log.e(TAG, "DynamicColorLoader: ", e)
                mResourceCodec = null
            }
        }
        if (!TextUtils.isEmpty(mColorData?.audioPath)) {
            mWeakFilter.get()?.let {
                it.setAudioPath(Uri.parse("$mFolderPath/${mColorData!!.audioPath}"))
                it.setLooping(mColorData!!.audioLooping)
            }
        }
        loadColorTexture()
    }

    /** 加载纹理 */
    private fun loadColorTexture() {
        if (mColorData?.uniformDataList.isNullOrEmpty()) {
            return
        }
        mTextureList = IntArray(mColorData!!.uniformDataList.size)
        for ((dataIndex, data) in mColorData!!.uniformDataList.withIndex()) {
            var bitmap: Bitmap? = null
            if (mResourceCodec != null) {
                bitmap = mResourceCodec!!.loadBitmap(data.value)
            }
            if (bitmap == null) {
                bitmap = BitmapUtils.getBitmapFromFile("$mFolderPath/${data.value}")
            }
            mTextureList!![dataIndex] = if (bitmap != null) {
                val tex = OpenGLUtils.createTexture(bitmap)
                bitmap.recycle()
                tex
            } else {
                OpenGLUtils.GL_NOT_TEXTURE
            }
        }
    }

    /** 绑定统一变量句柄 */
    fun onBindUniformHandle(programHandle: Int) {
        if (programHandle == OpenGLUtils.GL_NOT_INIT || mColorData == null) {
            return
        }
        mStrengthHandle = GLES30.glGetUniformLocation(programHandle, "strength")
        if (mColorData!!.texelOffset) {
            mTexelWidthOffsetHandle = GLES30.glGetUniformLocation(programHandle, "texelWidthOffset")
            mTexelHeightOffsetHandle = GLES30.glGetUniformLocation(programHandle, "texelHeightOffset")
        } else {
            mTexelWidthOffsetHandle = OpenGLUtils.GL_NOT_INIT
            mTexelHeightOffsetHandle = OpenGLUtils.GL_NOT_INIT
        }
        for (uniformString in mColorData!!.uniformList) {
            val handle = GLES30.glGetUniformLocation(programHandle, uniformString)
            mUniformHandleList[uniformString] = handle
        }
    }

    /** 输入纹理大小 */
    fun onInputSizeChange(width: Int, height: Int) {
        mTexelWidthOffset = 1.0f / width
        mTexelHeightOffset = 1.0f / height
    }

    /** 绑定滤镜纹理，只需要绑定一次就行，不用重复绑定，减少开销 */
    fun onDrawFrameBegin() {
        if (mStrengthHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mStrengthHandle, mStrength)
        }
        if (mTexelWidthOffsetHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mTexelWidthOffsetHandle, mTexelWidthOffset)
        }
        if (mTexelHeightOffsetHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mTexelHeightOffsetHandle, mTexelHeightOffset)
        }

        if (mTextureList == null || mColorData == null) {
            return
        }
        for ((dataIndex, data) in mColorData!!.uniformDataList.withIndex()) {
            val handle = mUniformHandleList[data.uniform]
            if (handle != null && mTextureList!![dataIndex] != OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.bindTexture(handle, mTextureList!![dataIndex], dataIndex + 1)
            }
        }
    }

    /** 释放资源 */
    fun release() {
        if (mTextureList != null && mTextureList!!.isNotEmpty()) {
            GLES30.glDeleteTextures(mTextureList!!.size, mTextureList, 0)
            mTextureList = null
        }
        mWeakFilter.clear()
    }

    /** 设置强度 */
    fun setStrength(strength: Float) {
        mStrength = strength
    }
}
