package com.cgfay.filter.glfilter.adjust.bean

/**
 * 调节参数
 */
class AdjustParam {
    // 亮度值 -1.0f ~ 1.0f
    var brightness: Float = 0.0f

    // 对比度 0.0 ~ 4.0f
    var contrast: Float = 1.0f

    // 曝光 -10.0f ~ 10.0f
    var exposure: Float = 0.0f

    // 色调 0 ~ 360
    var hue: Float = 0.0f

    // 饱和度 0 ~ 2.0f
    var saturation: Float = 1.0f

    // 锐度 -4.0f ~ 4.0f
    var sharpness: Float = 0.0f
}
