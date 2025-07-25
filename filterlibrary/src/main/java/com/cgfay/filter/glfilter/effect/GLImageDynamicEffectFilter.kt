package com.cgfay.filter.glfilter.effect

import android.content.Context
import android.text.TextUtils
import com.cgfay.filter.glfilter.base.GLImageGroupFilter
import com.cgfay.filter.glfilter.effect.bean.DynamicEffect

/**
 * Filter applying a list of dynamic effects.
 */
class GLImageDynamicEffectFilter(context: Context, dynamicEffect: DynamicEffect?) :
    GLImageGroupFilter(context) {

    init {
        if (dynamicEffect == null || dynamicEffect.effectList == null ||
            TextUtils.isEmpty(dynamicEffect.unzipPath)
        ) {
            return
        }
        // add filters
        for (data in dynamicEffect.effectList) {
            mFilters.add(DynamicEffectFilter(context, data, dynamicEffect.unzipPath))
        }
    }

    /**
     * Update time stamp for all child filters.
     */
    fun setTimeStamp(timeStamp: Float) {
        for (filter in mFilters) {
            if (filter is DynamicEffectBaseFilter) {
                filter.setTimeStamp(timeStamp)
            }
        }
    }
}
