package com.cgfay.filter.glfilter.effect

import android.content.Context
import com.cgfay.filter.glfilter.effect.bean.DynamicEffectData

/**
 * Simple wrapper filter for a dynamic effect.
 */
class DynamicEffectFilter(
    context: Context,
    dynamicEffectData: DynamicEffectData?,
    unzipPath: String
) : DynamicEffectBaseFilter(context, dynamicEffectData, unzipPath)
