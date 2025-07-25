package com.cgfay.filter.glfilter.makeup

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.util.Log
import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData
import com.cgfay.filter.glfilter.makeup.bean.MakeupLipstickData
import com.cgfay.filter.glfilter.makeup.bean.MakeupNormaData
import com.cgfay.filter.glfilter.resource.ResourceCodec
import com.cgfay.filter.glfilter.resource.ResourceDataCodec
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Base loader for makeup data.
 */
abstract class MakeupBaseLoader(
    filter: GLImageMakeupFilter,
    var makeupData: MakeupBaseData?,
    folderPath: String
) {
    protected val TAG = "MakeupLoader"

    protected var mImageWidth = 0
    protected var mImageHeight = 0
    protected var mStrength: Float = makeupData?.strength ?: 1.0f
    protected var mMakeupType = 0
    private var mFolderPath: String = if (folderPath.startsWith("file://")) folderPath.substring("file://".length) else folderPath
    private var mResourceCodec: ResourceDataCodec? = null
    protected var mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE
    protected var mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
    protected var mVertices: FloatArray? = null
    protected var mVertexBuffer: FloatBuffer? = null
    protected var mTextureBuffer: FloatBuffer? = null
    protected var mIndexBuffer: ShortBuffer? = null
    private var mEnableRender = false

    protected val mWeakFilter = WeakReference(filter)

    init {
        initBuffers()
    }

    /** Initialize resources. */
    open fun init(context: Context) {
        if (makeupData != null) {
            mEnableRender = true
            when (makeupData!!.makeupType) {
                // no mask
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.SHADOW,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.BLUSH,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYEBROW -> {
                    mMakeupType = 1
                    mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE
                }
                // pupil uses eye mask
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.PUPIL -> {
                    mMakeupType = 2
                    if (mMaskTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                        mMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_eye_mask.png")
                    }
                }
                // eye area
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYESHADOW,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELINER,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELASH,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELID -> {
                    mMakeupType = 1
                    if (mMaskTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                        mMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_eye_mask.png")
                    }
                }
                // lips
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.LIPSTICK -> {
                    mMakeupType = 3
                    if (mMaskTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                        mMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_lips_mask.png")
                    }
                }
                else -> {
                    mMakeupType = 0
                    mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE
                    mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
                }
            }
            loadMaterialTexture(mFolderPath)
        } else {
            mEnableRender = false
            mMakeupType = 0
            mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
    }

    /** Load material texture. */
    protected fun loadMaterialTexture(unzipPath: String) {
        if (mResourceCodec != null) {
            mResourceCodec = null
        }
        if (makeupData == null) {
            if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                GLES30.glDeleteTextures(1, intArrayOf(mMaterialTexture), 0)
                mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
            }
            return
        }
        val pair = ResourceCodec.getResourceFile(unzipPath)
        if (pair != null) {
            mResourceCodec = ResourceDataCodec(unzipPath + "/" + pair.first, unzipPath + "/" + pair.second)
        }
        if (mResourceCodec != null) {
            try {
                mResourceCodec!!.init()
            } catch (e: IOException) {
                Log.e(TAG, "loadMaterialTexture: ", e)
                mResourceCodec = null
            }
        }
        var bitmap: Bitmap? = null
        if (makeupData!!.makeupType.typeName == "lipstick") {
            bitmap = mResourceCodec?.loadBitmap((makeupData as MakeupLipstickData).lookupTable)
        } else if ((makeupData as MakeupNormaData).materialData != null) {
            bitmap = mResourceCodec?.loadBitmap((makeupData as MakeupNormaData).materialData!!.name)
        }
        if (bitmap != null) {
            if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                GLES30.glDeleteTextures(1, intArrayOf(mMaterialTexture), 0)
                mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
            }
            mMaterialTexture = OpenGLUtils.createTexture(bitmap)
            bitmap.recycle()
        } else {
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
    }

    open fun onInputSizeChanged(width: Int, height: Int) {
        mImageWidth = width
        mImageHeight = height
    }

    open fun reset() {
        if (mMaskTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, intArrayOf(mMaskTexture), 0)
            mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
        if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, intArrayOf(mMaterialTexture), 0)
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
        mEnableRender = false
    }

    open fun release() {
        if (mMaskTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, intArrayOf(mMaskTexture), 0)
            mMaskTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
        if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, intArrayOf(mMaterialTexture), 0)
            mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
        mWeakFilter.clear()
        releaseBuffers()
    }

    protected abstract fun initBuffers()

    protected fun releaseBuffers() {
        mVertexBuffer?.clear(); mVertexBuffer = null
        mTextureBuffer?.clear(); mTextureBuffer = null
        mIndexBuffer?.clear(); mIndexBuffer = null
    }

    open fun drawMakeup(faceIndex: Int, inputTexture: Int, vertexBuffer: FloatBuffer?, textureBuffer: FloatBuffer?) {
        updateVertices(faceIndex)
        if (mWeakFilter.get() != null && mEnableRender) {
            mWeakFilter.get()!!.drawMakeup(
                inputTexture,
                mMaterialTexture,
                mMaskTexture,
                mVertexBuffer,
                mTextureBuffer,
                mIndexBuffer,
                mMakeupType,
                mStrength
            )
        }
    }

    protected abstract fun updateVertices(faceIndex: Int)

    open fun changeMakeupData(makeupData: MakeupBaseData?, folderPath: String) {
        this.makeupData = makeupData
        mFolderPath = if (folderPath.startsWith("file://")) folderPath.substring("file://".length) else folderPath
        if (this.makeupData != null) {
            mStrength = this.makeupData!!.strength
            loadMaterialTexture(mFolderPath)
        } else {
            mStrength = 0f
            if (mMaterialTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                GLES30.glDeleteTextures(1, intArrayOf(mMaterialTexture), 0)
                mMaterialTexture = OpenGLUtils.GL_NOT_TEXTURE
            }
        }
    }

    fun setStrength(strength: Float) {
        mStrength = when {
            strength < 0f -> 0f
            strength > 1f -> 1f
            else -> strength
        }
    }

    fun resetStrength() {
        mStrength = makeupData?.strength ?: 1f
    }
}
