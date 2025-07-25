package com.cgfay.filter.glfilter.makeup

import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.landmark.LandmarkEngine

/**
 * Loader for normal makeup parts, excluding pupils.
 */
class MakeupNormalLoader(
    filter: GLImageMakeupFilter,
    makeupData: MakeupBaseData?,
    folderPath: String
) : MakeupBaseLoader(filter, makeupData, folderPath) {

    override fun initBuffers() {
        if (makeupData == null) return
        when (makeupData!!.makeupType) {
            // TODO shadow
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.SHADOW -> {}
            // eye region
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYESHADOW,
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELINER,
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELASH,
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELID,
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYEBROW -> {
            }
            // blush
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.BLUSH -> {}
            // lipstick
            com.cgfay.filter.glfilter.makeup.bean.MakeupType.LIPSTICK -> {
                mVertices = FloatArray(40)
                mVertexBuffer = OpenGLUtils.createFloatBuffer(mVertices)
                mTextureBuffer = OpenGLUtils.createFloatBuffer(MakeupVertices.lipsMaskTextureVertices)
                mIndexBuffer = OpenGLUtils.createShortBuffer(MakeupVertices.lipsIndices)
            }
            else -> {}
        }
    }

    override fun updateVertices(faceIndex: Int) {
        if (mVertexBuffer == null || mVertices == null) return
        mVertexBuffer!!.clear()
        if (LandmarkEngine.getInstance().hasFace() && LandmarkEngine.getInstance().faceSize > faceIndex) {
            when (makeupData!!.makeupType) {
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.SHADOW -> LandmarkEngine.getInstance().getShadowVertices(mVertices, faceIndex)
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYESHADOW,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELINER,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELASH,
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYELID -> LandmarkEngine.getInstance().getEyeVertices(mVertices, faceIndex)
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.EYEBROW -> LandmarkEngine.getInstance().getEyeBrowVertices(mVertices, faceIndex)
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.BLUSH -> LandmarkEngine.getInstance().getBlushVertices(mVertices, faceIndex)
                com.cgfay.filter.glfilter.makeup.bean.MakeupType.LIPSTICK -> LandmarkEngine.getInstance().getLipsVertices(mVertices, faceIndex)
                else -> {}
            }
            mVertexBuffer!!.put(mVertices)
        }
        mVertexBuffer!!.position(0)
    }
}
