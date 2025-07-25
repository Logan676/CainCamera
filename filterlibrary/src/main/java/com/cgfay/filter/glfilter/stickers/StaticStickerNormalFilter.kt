package com.cgfay.filter.glfilter.stickers

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.stickers.bean.StaticStickerNormalData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.nio.FloatBuffer

/**
 * 实现静态或者动态贴纸，并且实现其旋转，移动，缩放
 */
class StaticStickerNormalFilter(context: Context, sticker: DynamicSticker?) :
    DynamicStickerBaseFilter(
        context,
        sticker,
        OpenGLUtils.getShaderFromAssets(context, "shader/sticker/vertex_sticker_normal.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/sticker/fragment_sticker_normal.glsl")
    ) {

    private var mMVPMatrixHandle = 0
    private val transformMatrix = Matrix4()
    private val projectionMatrix = Matrix4()
    private val combinedMatrix = Matrix4()
    var camera: OrthographicCamera = OrthographicCamera()

    private var mVertexBuffer: FloatBuffer? = null
    private var mTextureBuffer: FloatBuffer? = null
    private var mVideoVertexBuffer: FloatBuffer? = null

    init {
        if (mDynamicSticker != null && mDynamicSticker!!.dataList != null) {
            for (i in mDynamicSticker!!.dataList.indices) {
                if (mDynamicSticker!!.dataList[i] is StaticStickerNormalData) {
                    val path = mDynamicSticker!!.unzipPath + "/" + mDynamicSticker!!.dataList[i].stickerName
                    mStickerLoaderList.add(
                        DynamicStickerLoader(true, this, mDynamicSticker!!.dataList[i], path)
                    )
                }
            }
        }
        camera = OrthographicCamera()
        initBuffer()
    }

    private fun initBuffer() {
        releaseBuffer()
        mVideoVertexBuffer = OpenGLUtils.createFloatBuffer(mVideoVertices)
        mVertexBuffer = OpenGLUtils.createFloatBuffer(mStickerVertices)
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
    }

    private fun releaseBuffer() {
        mVertexBuffer?.clear(); mVertexBuffer = null
        mVideoVertexBuffer?.clear(); mVideoVertexBuffer = null
        mTextureBuffer?.clear(); mTextureBuffer = null
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        mMVPMatrixHandle = if (mProgramHandle != OpenGLUtils.GL_NOT_INIT)
            GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
        else OpenGLUtils.GL_NOT_INIT
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
        camera.update()
        projectionMatrix.set(camera.combined)
        updateVideoVertexBuffer(width.toFloat(), height.toFloat())
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        camera.setGdxGraphicsSize(width, height)
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        val stickerTexture = drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        return super.drawFrame(stickerTexture, vertexBuffer, textureBuffer)
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        transformMatrix.idt()
        super.drawFrameBuffer(textureId, mVideoVertexBuffer!!, textureBuffer)
        if (mStickerLoaderList.isNotEmpty()) {
            for (stickerLoader in mStickerLoaderList) {
                synchronized(this) {
                    stickerLoader.updateStickerTexture()
                    calculateStickerVertices(stickerLoader.getStickerData() as StaticStickerNormalData)
                    super.drawFrameBuffer(stickerLoader.getStickerTexture(), mVertexBuffer!!, mTextureBuffer!!)
                }
            }
        }
        return mFrameBufferTextures[0]
    }

    private val mStickerVertices = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )
    private val mVideoVertices = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )

    fun updateVideoVertexBuffer(width: Float, height: Float) {
        val stickerHeight = height
        val stickerWidth = width
        val stickerX = 0f
        val stickerY = 0f
        mVideoVertices[0] = stickerX; mVideoVertices[1] = stickerY
        mVideoVertices[2] = stickerX + stickerWidth; mVideoVertices[3] = stickerY
        mVideoVertices[4] = stickerX; mVideoVertices[5] = stickerY + stickerHeight
        mVideoVertices[6] = stickerX + stickerWidth; mVideoVertices[7] = stickerY + stickerHeight
        mVideoVertexBuffer!!.clear();
        mVideoVertexBuffer!!.position(0);
        mVideoVertexBuffer!!.put(mVideoVertices);
    }

    private fun calculateStickerVertices(stickerData: StaticStickerNormalData) {
        swidth = stickerData.width.toFloat()
        sheight = stickerData.height.toFloat()
        val stickerHeight = stickerData.height.toFloat()
        val stickerWidth = stickerData.width.toFloat()
        val stickerX = x
        val stickerY = y
        mStickerVertices[0] = stickerX; mStickerVertices[1] = stickerY
        mStickerVertices[2] = stickerX + stickerWidth; mStickerVertices[3] = stickerY
        mStickerVertices[4] = stickerX; mStickerVertices[5] = stickerY + stickerHeight
        mStickerVertices[6] = stickerX + stickerWidth; mStickerVertices[7] = stickerY + stickerHeight
        mVertexBuffer!!.clear();
        mVertexBuffer!!.position(0);
        mVertexBuffer!!.put(mStickerVertices);
        transformMatrix.idt();
        val centerX = stickerX + stickerWidth / 2f
        val centerY = stickerY + stickerHeight / 2f
        transformMatrix.translate(centerX, centerY, 0f)
        transformMatrix.rotate(Vector3.Z, rotation.toFloat())
        transformMatrix.scale(scale, scale, scale)
        transformMatrix.translate(-centerX, -centerY, 0f)
        rotation += 1
        if (scale >= 1.2f) {
            flipScale = -1
        }
        if (scale <= 0.8f) {
            flipScale = 1
        }
        scale += 0.01f * flipScale
    }

    private var flipScale = 1
    private var rotation = 0
    private var scale = 1f
    private var x = 0f
    private var y = 0f
    private var swidth = 0f
    private var sheight = 0f

    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun setRotate(rotate: Int) {
        this.rotation = rotate
    }

    fun scale(scale: Float) {
        this.scale = scale
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        combinedMatrix.set(projectionMatrix).mul(transformMatrix)
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, combinedMatrix.val, 0)
        }
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE, GLES30.GL_ONE)
    }

    override fun onDrawFrameAfter() {
        super.onDrawFrameAfter()
        GLES30.glDisable(GLES30.GL_BLEND)
    }

    override fun release() {
        super.release()
        for (loader in mStickerLoaderList) {
            loader.release()
        }
        mStickerLoaderList.clear()
    }

    fun parentToLocalCoordinates(parentCoords: Vector3): Vector3 {
        val rot = this.rotation.toFloat()
        val scaleX = this.scale
        val scaleY = this.scale
        val childX = x
        val childY = y
        val originX = swidth / 2f
        val originY = sheight / 2f
        if (rot == 0f) {
            if (scaleX == 1f && scaleY == 1f) {
                parentCoords.x -= childX
                parentCoords.y -= childY
            } else {
                parentCoords.x = (parentCoords.x - childX - originX) / scaleX + originX
                parentCoords.y = (parentCoords.y - childY - originY) / scaleY + originY
            }
        } else {
            val cos = Math.cos(rot * MathUtils.degreesToRadians.toDouble()).toFloat()
            val sin = Math.sin(rot * MathUtils.degreesToRadians.toDouble()).toFloat()
            val tox = parentCoords.x - childX - originX
            val toy = parentCoords.y - childY - originY
            parentCoords.x = (tox * cos + toy * sin) / scaleX + originX
            parentCoords.y = (tox * -sin + toy * cos) / scaleY + originY
        }
        return parentCoords
    }

    fun hit(target: Vector3): StaticStickerNormalFilter? {
        parentToLocalCoordinates(target)
        return if (target.x >= 0 && target.x < swidth && target.y >= 0 && target.y < sheight) this else null
    }

    private val tempVec = Vector3()
    private val tmpCoords3 = Vector3()
    fun onScroll(distanceX: Float, distanceY: Float) {
        stageToLocalAmount(tempVec.set(distanceX, distanceY, 0f))
        Log.d("sticker", "onscrollx=" + tempVec.x + ",onscrolly=" + tempVec.y)
        setPosition(x - tempVec.x, y - tempVec.y)
    }

    private fun stageToLocalAmount(amount: Vector3) {
        camera.unproject(amount)
        amount.sub(camera.unproject(tmpCoords3.set(0f, 0f, 0f)))
    }
}
