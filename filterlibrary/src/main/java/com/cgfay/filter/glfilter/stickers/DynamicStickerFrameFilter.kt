package com.cgfay.filter.glfilter.stickers

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerFrameData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/**
 * 前景贴纸滤镜
 */
class DynamicStickerFrameFilter(context: Context, sticker: DynamicSticker?) :
    DynamicStickerBaseFilter(
        context,
        sticker,
        OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_frame.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_frame.glsl")
    ) {

    private var mStickerCoordHandle = 0
    private var mStickerTextureHandle = 0
    private var mEnableStickerHandle = 0
    private val mStickerBuffer: FloatBuffer = OpenGLUtils.createFloatBuffer(
        floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
        )
    )
    private var mStickerTexture = OpenGLUtils.GL_NOT_TEXTURE

    init {
        if (mDynamicSticker != null && mDynamicSticker!!.dataList != null) {
            for (i in mDynamicSticker!!.dataList.indices) {
                if (mDynamicSticker!!.dataList[i] is DynamicStickerFrameData) {
                    val path = mDynamicSticker!!.unzipPath + "/" + mDynamicSticker!!.dataList[i].stickerName
                    mStickerLoaderList.add(
                        DynamicStickerLoader(this, mDynamicSticker!!.dataList[i], path)
                    )
                }
            }
        }
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mStickerCoordHandle = GLES30.glGetAttribLocation(mProgramHandle, "aStickerCoord")
            mStickerTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "stickerTexture")
            mEnableStickerHandle = GLES30.glGetUniformLocation(mProgramHandle, "enableSticker")
        }
    }

    override fun drawFrame(
        textureId: Int,
        vertexBuffer: FloatBuffer,
        textureBuffer: FloatBuffer
    ): Boolean {
        for (loader in mStickerLoaderList) {
            loader.updateStickerTexture()
            mStickerTexture = loader.getStickerTexture()
        }
        return super.drawFrame(textureId, vertexBuffer, textureBuffer)
    }

    override fun drawFrameBuffer(
        textureId: Int,
        vertexBuffer: FloatBuffer,
        textureBuffer: FloatBuffer
    ): Int {
        for (loader in mStickerLoaderList) {
            loader.updateStickerTexture()
            mStickerTexture = loader.getStickerTexture()
        }
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        mStickerBuffer.position(0)
        GLES30.glVertexAttribPointer(
            mStickerCoordHandle,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            mStickerBuffer
        )
        GLES30.glEnableVertexAttribArray(mStickerCoordHandle)
        if (mStickerTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mStickerTextureHandle, mStickerTexture, 1)
            GLES30.glUniform1i(mEnableStickerHandle, 1)
        } else {
            GLES30.glUniform1i(mEnableStickerHandle, 0)
        }
    }

    override fun onDrawFrameAfter() {
        super.onDrawFrameAfter()
        GLES30.glDisableVertexAttribArray(mStickerCoordHandle)
    }

    override fun release() {
        super.release()
        for (loader in mStickerLoaderList) {
            loader.release()
        }
        mStickerLoaderList.clear()
    }
}
