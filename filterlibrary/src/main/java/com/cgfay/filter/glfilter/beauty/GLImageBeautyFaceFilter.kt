package com.cgfay.filter.glfilter.beauty

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageDrawElementsFilter
import com.cgfay.filter.glfilter.base.GLImageGaussianBlurFilter
import com.cgfay.filter.glfilter.beauty.bean.BeautyParam
import com.cgfay.filter.glfilter.beauty.bean.IBeautify
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import com.cgfay.landmark.LandmarkEngine
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Filter performing eye brightening, teeth whitening and other face enhancements.
 */
class GLImageBeautyFaceFilter(context: Context) :
    GLImageDrawElementsFilter(
        context,
        OpenGLUtils.getShaderFromAssets(context, "shader/beauty/vertex_beauty_face.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/beauty/fragment_beauty_face.glsl")
    ), IBeautify {

    private val vertices = FloatArray(MaxLength)
    private var blurFilter: GLImageGaussianBlurFilter? = GLImageGaussianBlurFilter(context).apply { setBlurSize(1.0f) }
    private var blurNextFilter: GLImageGaussianBlurFilter? = GLImageGaussianBlurFilter(context).apply { setBlurSize(0.3f) }

    private var blurTextureHandle = 0
    private var blurTexture2Handle = 0
    private var maskTextureHandle = 0
    private var teethLookupTextureHandle = 0

    private var brightEyeStrengthHandle = 0
    private var teethStrengthHandle = 0
    private var nasolabialStrengthHandle = 0
    private var furrowStrengthHandle = 0
    private var eyeBagStrengthHandle = 0
    private var processTypeHandle = 0

    private var vertexBuffer: FloatBuffer? = null
    private var maskTextureBuffer: FloatBuffer? = null

    private var blurTexture = OpenGLUtils.GL_NOT_TEXTURE
    private var blurTexture2 = OpenGLUtils.GL_NOT_TEXTURE

    private val eyeMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/makeup_eye_mask.png")
    private val teethMaskTexture = OpenGLUtils.createTextureFromAssets(context, "texture/teeth_mask.png")
    private val teethLookupTexture = OpenGLUtils.createTextureFromAssets(context, "texture/teeth_beauty_lookup.png")

    private var brightEyeStrength = 0f
    private var beautyTeethStrength = 0f
    private var nasolabialStrength = 0f
    private var furrowStrength = 0f
    private var eyeBagStrength = 0f

    private var processType = 0

    override fun initBuffers() {
        releaseBuffers()
        vertexBuffer = ByteBuffer.allocateDirect(MaxLength * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { position(0) }
        maskTextureBuffer = ByteBuffer.allocateDirect(MaxLength * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { position(0) }
        mIndexBuffer = ByteBuffer.allocateDirect(MaxLength * 2).order(ByteOrder.nativeOrder()).asShortBuffer().apply { position(0) }
    }

    override fun releaseBuffers() {
        super.releaseBuffers()
        vertexBuffer?.clear(); vertexBuffer = null
        maskTextureBuffer?.clear(); maskTextureBuffer = null
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            blurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture")
            blurTexture2Handle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture2")
            maskTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "maskTexture")
            teethLookupTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "teethLookupTexture")

            brightEyeStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "brightEyeStrength")
            teethStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "teethStrength")
            nasolabialStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "nasolabialStrength")
            furrowStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "furrowStrength")
            eyeBagStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "eyeBagStrength")
            processTypeHandle = GLES30.glGetUniformLocation(mProgramHandle, "processType")
        }
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        blurFilter?.onInputSizeChanged((width / 3.0f).toInt(), (height / 3.0f).toInt())
        blurNextFilter?.onInputSizeChanged((width / 3.0f).toInt(), (height / 3.0f).toInt())
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        blurFilter?.onDisplaySizeChanged(width, height)
        blurNextFilter?.onDisplaySizeChanged(width, height)
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        setInteger(processTypeHandle, 0)
        updateBuffer(0, -1)
        super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)

        if (LandmarkEngine.getInstance().hasFace()) {
            blurFilter?.let { blurTexture = it.drawFrameBuffer(textureId, vertexBuffer, textureBuffer) }
            blurNextFilter?.let { blurTexture2 = it.drawFrameBuffer(textureId, vertexBuffer, textureBuffer) }
            for (faceIndex in 0 until LandmarkEngine.getInstance().faceSize) {
                if (brightEyeStrength != 0f) {
                    updateBuffer(1, faceIndex)
                    setInteger(processTypeHandle, 1)
                    setFloat(brightEyeStrengthHandle, brightEyeStrength)
                    super.drawFrameBuffer(textureId, this.vertexBuffer, maskTextureBuffer)
                }
                if (beautyTeethStrength != 0f) {
                    updateBuffer(2, faceIndex)
                    setInteger(processTypeHandle, 2)
                    setFloat(teethStrengthHandle, beautyTeethStrength)
                    super.drawFrameBuffer(textureId, this.vertexBuffer, maskTextureBuffer)
                }
                // TODO: Other processing types not implemented yet
            }
        }
        return mFrameBufferTextures[0]
    }

    private fun updateBuffer(type: Int, faceIndex: Int) {
        processType = type
        when (type) {
            1 -> {
                LandmarkEngine.getInstance().getBrightEyeVertices(vertices, faceIndex)
                vertexBuffer!!.clear(); vertexBuffer!!.put(vertices); vertexBuffer!!.position(0)
                maskTextureBuffer!!.clear(); maskTextureBuffer!!.put(EyeMaskTextureVertices); maskTextureBuffer!!.position(0)
                mIndexBuffer!!.clear(); mIndexBuffer!!.put(EyeIndices); mIndexBuffer!!.position(0)
                mIndexLength = EyeIndices.size
            }
            2 -> {
                LandmarkEngine.getInstance().getBeautyTeethVertices(vertices, faceIndex)
                vertexBuffer!!.clear(); vertexBuffer!!.put(vertices); vertexBuffer!!.position(0)
                maskTextureBuffer!!.clear(); maskTextureBuffer!!.put(TeethMaskTextureVertices); maskTextureBuffer!!.position(0)
                mIndexBuffer!!.clear(); mIndexBuffer!!.put(TeethIndices); mIndexBuffer!!.position(0)
                mIndexLength = TeethIndices.size
            }
            else -> {
                mIndexBuffer!!.clear(); mIndexBuffer!!.put(TextureRotationUtils.Indices); mIndexBuffer!!.position(0)
                mIndexLength = 6
            }
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        if (blurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(blurTextureHandle, blurTexture, 1)
        }
        if (blurTexture2 != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(blurTexture2Handle, blurTexture2, 2)
        }
        when (processType) {
            1 -> OpenGLUtils.bindTexture(maskTextureHandle, eyeMaskTexture, 3)
            2 -> OpenGLUtils.bindTexture(maskTextureHandle, teethMaskTexture, 3)
        }
        OpenGLUtils.bindTexture(teethLookupTextureHandle, teethLookupTexture, 4)
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer(width, height)
        blurFilter?.initFrameBuffer((width / 3.0f).toInt(), (height / 3.0f).toInt())
        blurNextFilter?.initFrameBuffer((width / 3.0f).toInt(), (height / 3.0f).toInt())
    }

    override fun destroyFrameBuffer() {
        super.destroyFrameBuffer()
        blurFilter?.destroyFrameBuffer()
        blurNextFilter?.destroyFrameBuffer()
    }

    override fun release() {
        super.release()
        blurFilter?.release(); blurFilter = null
        blurNextFilter?.release(); blurNextFilter = null
    }

    override fun onBeauty(beauty: BeautyParam) {
        brightEyeStrength = GLImageFilter.clamp(beauty.eyeBrightIntensity, 0.0f, 1.0f)
        beautyTeethStrength = GLImageFilter.clamp(beauty.teethBeautyIntensity, 0.0f, 1.0f)
        nasolabialStrength = GLImageFilter.clamp(beauty.nasolabialFoldsIntensity, 0.0f, 1.0f)
        furrowStrength = GLImageFilter.clamp(beauty.eyeFurrowsIntensity, 0.0f, 1.0f)
        eyeBagStrength = GLImageFilter.clamp(beauty.eyeBagsIntensity, 0.0f, 1.0f)
    }

    companion object {
        private const val MaxLength = 100

        private val EyeIndices = shortArrayOf(
            0, 5, 1,
            1, 5, 12,
            12, 5, 13,
            12, 13, 4,
            12, 4, 2,
            2, 4, 3,
            6, 7, 11,
            7, 11, 14,
            14, 11, 15,
            14, 15, 10,
            14, 10, 8,
            8, 10, 9
        )

        private val EyeMaskTextureVertices = floatArrayOf(
            0.102757f, 0.465517f,
            0.175439f, 0.301724f,
            0.370927f, 0.310345f,
            0.446115f, 0.603448f,
            0.353383f, 0.732759f,
            0.197995f, 0.689655f,
            0.566416f, 0.629310f,
            0.659148f, 0.336207f,
            0.802005f, 0.318966f,
            0.884712f, 0.465517f,
            0.812030f, 0.681034f,
            0.681704f, 0.750023f,
            0.273183f, 0.241379f,
            0.275689f, 0.758620f,
            0.721805f, 0.275862f,
            0.739348f, 0.758621f
        )

        private val TeethIndices = shortArrayOf(
            0, 11, 1,
            1, 11, 10,
            1, 10, 2,
            2, 10, 3,
            3, 10, 9,
            3, 9, 8,
            3, 8, 4,
            4, 8, 5,
            5, 8, 7,
            5, 7, 6
        )

        private val TeethMaskTextureVertices = floatArrayOf(
            0.154639f, 0.378788f,
            0.295533f, 0.287879f,
            0.398625f, 0.196970f,
            0.512027f, 0.287879f,
            0.611684f, 0.212121f,
            0.728523f, 0.287879f,
            0.872852f, 0.378788f,
            0.742268f, 0.704546f,
            0.639176f, 0.848485f,
            0.522337f, 0.636364f,
            0.398625f, 0.833333f,
            0.240550f, 0.651515f
        )
    }
}
