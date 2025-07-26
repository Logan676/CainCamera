package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES30
import java.nio.FloatBuffer
import java.util.ArrayList

/**
 * Base class for a group of filters.
 */
abstract class GLImageGroupFilter : GLImageFilter {
    protected var mFilters: MutableList<GLImageFilter> = ArrayList()

    constructor(context: Context) : super(context, null, null)

    protected constructor(context: Context, filters: List<GLImageFilter>?) : super(context, null, null) {
        if (filters != null) {
            mFilters.addAll(filters)
        }
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        for (filter in mFilters) {
            filter.onInputSizeChanged(width, height)
        }
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        for (filter in mFilters) {
            filter.onDisplaySizeChanged(width, height)
        }
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        if (mFilters.isEmpty()) return false
        var result = super.drawFrame(textureId, vertexBuffer, textureBuffer)
        var currentTexture = textureId
        val size = mFilters.size
        for (i in 0 until size) {
            val filter = mFilters[i]
            if (i == size - 1) {
                GLES30.glViewport(0, 0, filter.getDisplayWidth(), filter.getDisplayHeight())
                result = filter.drawFrame(currentTexture, vertexBuffer, textureBuffer)
            } else {
                currentTexture = filter.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
        }
        return result
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        if (mFilters.isEmpty()) return textureId
        var currentTexture = textureId
        for (filter in mFilters) {
            currentTexture = filter.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        }
        return currentTexture
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer(width, height)
        for (filter in mFilters) {
            filter.initFrameBuffer(width, height)
        }
    }

    override fun release() {
        super.release()
        for (filter in mFilters) {
            filter.release()
        }
        mFilters.clear()
    }
}

