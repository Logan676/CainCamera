package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 镜像翻转
 */
class GLImageMirrorFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_mirror.glsl")
) {

    private var angleHandle = 0
    private var mirrorXHandle = 0
    private var mirrorYHandle = 0
    private var angle = 0f
    private var mirrorX = 0f
    private var mirrorY = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        angleHandle = GLES30.glGetUniformLocation(mProgramHandle, "Angle")
        mirrorXHandle = GLES30.glGetUniformLocation(mProgramHandle, "MirrorX")
        mirrorYHandle = GLES30.glGetUniformLocation(mProgramHandle, "MirrorY")
        setAngle(0f)
        setMirrorX(0f)
        setMirrorY(0f)
    }

    /** 设置旋转角度 */
    fun setAngle(angle: Float) {
        this.angle = angle
        setFloat(angleHandle, this.angle)
    }

    /** x坐标 */
    fun setMirrorX(mirrorX: Float) {
        this.mirrorX = mirrorX
        setFloat(mirrorXHandle, this.mirrorX)
    }

    /** y坐标 */
    fun setMirrorY(mirrorY: Float) {
        this.mirrorY = mirrorY
        setFloat(mirrorYHandle, this.mirrorY)
    }
}
