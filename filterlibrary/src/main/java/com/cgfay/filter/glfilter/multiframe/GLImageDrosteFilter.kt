package com.cgfay.filter.glfilter.multiframe

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Droste effect filter implemented in Kotlin.
 */
class GLImageDrosteFilter @JvmOverloads constructor(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/multiframe/fragment_droste.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var mRepeatHandle = 0
    private var repeat = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mRepeatHandle = GLES30.glGetUniformLocation(mProgramHandle, "repeat")
            setRepeat(4)
        }
    }

    /**
     * Set number of texture repeats.
     */
    fun setRepeat(repeat: Int) {
        this.repeat = repeat.toFloat()
        setFloat(mRepeatHandle, this.repeat)
    }
}
