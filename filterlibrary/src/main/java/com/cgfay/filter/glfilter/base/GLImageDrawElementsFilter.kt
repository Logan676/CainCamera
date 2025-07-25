package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.nio.ShortBuffer

open class GLImageDrawElementsFilter : GLImageFilter {
    protected var mIndexBuffer: ShortBuffer? = null
    protected var mIndexLength = 0

    constructor(context: Context) : this(context, VERTEX_SHADER, FRAGMENT_SHADER)

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader) {
        initBuffers()
    }

    protected fun initBuffers() {
        releaseBuffers()
        mIndexBuffer = OpenGLUtils.createShortBuffer(TextureRotationUtils.Indices)
        mIndexLength = 6
    }

    protected fun releaseBuffers() {
        mIndexBuffer?.clear()
        mIndexBuffer = null
    }

    override fun onDrawFrame() {
        if (mIndexBuffer != null) {
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndexLength, GLES30.GL_UNSIGNED_SHORT, mIndexBuffer)
        } else {
            super.onDrawFrame()
        }
    }

    override fun release() {
        super.release()
        releaseBuffers()
    }
}
