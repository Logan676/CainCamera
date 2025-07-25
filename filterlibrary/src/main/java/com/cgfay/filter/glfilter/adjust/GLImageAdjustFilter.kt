package com.cgfay.filter.glfilter.adjust

import android.content.Context
import com.cgfay.filter.glfilter.adjust.bean.AdjustParam
import com.cgfay.filter.glfilter.adjust.bean.IAdjust
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageGroupFilter

/**
 * 调节滤镜
 */
class GLImageAdjustFilter(context: Context) :
    GLImageGroupFilter(context, initFilters(context)), IAdjust {

    companion object {
        // 滤镜索引
        private const val IndexBrightness = 0
        private const val IndexContrast = 1
        private const val IndexExposure = 2
        private const val IndexHue = 3
        private const val IndexSaturation = 4
        private const val IndexSharpen = 5

        private fun initFilters(context: Context): List<GLImageFilter> {
            val filters = ArrayList<GLImageFilter>()
            filters.add(IndexBrightness, GLImageBrightnessFilter(context))
            filters.add(IndexContrast, GLImageContrastFilter(context))
            filters.add(IndexExposure, GLImageExposureFilter(context))
            filters.add(IndexHue, GLImageHueFilter(context))
            filters.add(IndexSaturation, GLImageSaturationFilter(context))
            filters.add(IndexSharpen, GLImageSharpenFilter(context))
            return filters
        }
    }

    override fun onAdjust(adjust: AdjustParam) {
        mFilters[IndexBrightness]?.let {
            (it as GLImageBrightnessFilter).setBrightness(adjust.brightness)
        }
        mFilters[IndexContrast]?.let {
            (it as GLImageContrastFilter).setContrast(adjust.contrast)
        }
        mFilters[IndexExposure]?.let {
            (it as GLImageExposureFilter).setExposure(adjust.exposure)
        }
        mFilters[IndexHue]?.let {
            (it as GLImageHueFilter).setHue(adjust.hue)
        }
        mFilters[IndexSaturation]?.let {
            (it as GLImageSaturationFilter).setSaturation(adjust.saturation)
        }
        mFilters[IndexSharpen]?.let {
            (it as GLImageSharpenFilter).setSharpness(adjust.sharpness)
        }
    }
}
