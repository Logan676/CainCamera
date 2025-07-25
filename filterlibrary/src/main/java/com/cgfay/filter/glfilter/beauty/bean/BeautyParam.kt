package com.cgfay.filter.glfilter.beauty.bean

/**
 * Beauty parameters for filter adjustments.
 */
class BeautyParam {
    var beautyIntensity: Float = 0.5f
    var complexionIntensity: Float = 0.5f
    var faceLift: Float = 0.0f
    var faceShave: Float = 0.0f
    var faceNarrow: Float = 0.0f
    var chinIntensity: Float = 0.0f
    var nasolabialFoldsIntensity: Float = 0.0f
    var foreheadIntensity: Float = 0.0f
    var eyeEnlargeIntensity: Float = 0.0f
    var eyeDistanceIntensity: Float = 0.0f
    var eyeCornerIntensity: Float = 0.0f
    var eyeFurrowsIntensity: Float = 0.0f
    var eyeBagsIntensity: Float = 0.0f
    var eyeBrightIntensity: Float = 0.0f
    var noseThinIntensity: Float = 0.0f
    var alaeIntensity: Float = 0.0f
    var proboscisIntensity: Float = 0.0f
    var mouthEnlargeIntensity: Float = 0.0f
    var teethBeautyIntensity: Float = 0.0f

    /**
     * Reset all parameters to their default values.
     */
    fun reset() {
        beautyIntensity = 0.5f
        complexionIntensity = 0.5f
        faceLift = 0.0f
        faceShave = 0.0f
        faceNarrow = 0.0f
        chinIntensity = 0.0f
        nasolabialFoldsIntensity = 0.0f
        foreheadIntensity = 0.0f
        eyeEnlargeIntensity = 0.0f
        eyeDistanceIntensity = 0.0f
        eyeCornerIntensity = 0.0f
        eyeFurrowsIntensity = 0.0f
        eyeBagsIntensity = 0.0f
        eyeBrightIntensity = 0.0f
        noseThinIntensity = 0.0f
        alaeIntensity = 0.0f
        proboscisIntensity = 0.0f
        mouthEnlargeIntensity = 0.0f
        teethBeautyIntensity = 0.0f
    }
}
