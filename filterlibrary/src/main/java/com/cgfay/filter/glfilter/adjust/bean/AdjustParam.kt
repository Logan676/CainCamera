package com.cgfay.filter.glfilter.adjust.bean

/**
 * Parameters for filter adjustments.
 */
data class AdjustParam(
    // brightness value -1.0f ~ 1.0f
    var brightness: Float = 0.0f,
    // contrast value 0.0 ~ 4.0f
    var contrast: Float = 1.0f,
    // exposure value -10.0f ~ 10.0f
    var exposure: Float = 0.0f,
    // hue value 0 ~ 360
    var hue: Float = 0.0f,
    // saturation value 0 ~ 2.0f
    var saturation: Float = 1.0f,
    // sharpness value -4.0f ~ 4.0f
    var sharpness: Float = 0.0f
)
