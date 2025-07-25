package com.cgfay.filter.glfilter.color

import android.content.Context
import android.text.TextUtils
import com.cgfay.filter.glfilter.base.GLImageGroupFilter
import com.cgfay.filter.glfilter.color.bean.DynamicColor

/**
 * 颜色滤镜
 */
class GLImageDynamicColorFilter(context: Context, dynamicColor: DynamicColor?) : GLImageGroupFilter(context) {

    init {
        if (dynamicColor == null || dynamicColor.filterList == null || TextUtils.isEmpty(dynamicColor.unzipPath)) {
            return
        }
        for (i in dynamicColor.filterList.indices) {
            mFilters.add(DynamicColorFilter(context, dynamicColor.filterList[i], dynamicColor.unzipPath))
        }
    }

    /** 设置滤镜强度 */
    fun setStrength(strength: Float) {
        for (filter in mFilters) {
            if (filter is DynamicColorBaseFilter) {
                filter.setStrength(strength)
            }
        }
    }
}
