package com.cgfay.filter.glfilter.color

import android.content.Context
import android.text.TextUtils
import com.cgfay.filter.glfilter.base.GLImageAudioFilter
import com.cgfay.filter.glfilter.color.bean.DynamicColorData
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 颜色滤镜基类
 */
open class DynamicColorBaseFilter(
    context: Context,
    protected var mDynamicColorData: DynamicColorData?,
    unzipPath: String
) : GLImageAudioFilter(
    context,
    if (mDynamicColorData == null || TextUtils.isEmpty(mDynamicColorData!!.vertexShader)) VERTEX_SHADER
    else getShaderString(context, unzipPath, mDynamicColorData!!.vertexShader),
    if (mDynamicColorData == null || TextUtils.isEmpty(mDynamicColorData!!.fragmentShader)) FRAGMENT_SHADER
    else getShaderString(context, unzipPath, mDynamicColorData!!.fragmentShader)
) {

    protected var mDynamicColorLoader: DynamicColorLoader? = DynamicColorLoader(this, mDynamicColorData, unzipPath).apply {
        onBindUniformHandle(mProgramHandle)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        mDynamicColorLoader?.onInputSizeChange(width, height)
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        mDynamicColorLoader?.onDrawFrameBegin()
    }

    override fun release() {
        super.release()
        mDynamicColorLoader?.release()
    }

    /**
     * 设置强度，调节滤镜的轻重程度
     */
    fun setStrength(strength: Float) {
        mDynamicColorLoader?.setStrength(strength)
    }

    companion object {
        /** 根据解压路径和shader名称读取shader的字符串内容 */
        fun getShaderString(context: Context, unzipPath: String?, shaderName: String?): String {
            if (unzipPath.isNullOrEmpty() || shaderName.isNullOrEmpty()) {
                throw IllegalArgumentException("shader is empty!")
            }
            val path = "$unzipPath/$shaderName"
            return when {
                path.startsWith("assets://") -> {
                    OpenGLUtils.getShaderFromAssets(context, path.substring("assets://".length))
                }
                path.startsWith("file://") -> {
                    OpenGLUtils.getShaderFromFile(path.substring("file://".length))
                }
                else -> OpenGLUtils.getShaderFromFile(path)
            }
        }
    }
}
