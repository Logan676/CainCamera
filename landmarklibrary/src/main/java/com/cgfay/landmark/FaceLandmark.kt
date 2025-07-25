package com.cgfay.landmark

/**
 * Landmark indices (106 key points + 8 extended points)
 */
object FaceLandmark {
    // 左眉毛
    const val leftEyebrowRightCorner = 37
    const val leftEyebrowLeftCorner = 33
    const val leftEyebrowLeftTopCorner = 34
    const val leftEyebrowRightTopCorner = 36
    const val leftEyebrowUpperMiddle = 35
    const val leftEyebrowLowerMiddle = 65

    // 右眉毛
    const val rightEyebrowRightCorner = 38
    const val rightEyebrowLeftCorner = 42
    const val rightEyebrowLeftTopCorner = 39
    const val rightEyebrowRightTopCorner = 41
    const val rightEyebrowUpperMiddle = 40
    const val rightEyebrowLowerMiddle = 70

    // 左眼
    const val leftEyeTop = 72
    const val leftEyeCenter = 74
    const val leftEyeBottom = 73
    const val leftEyeLeftCorner = 52
    const val leftEyeRightCorner = 55

    // 右眼
    const val rightEyeTop = 75
    const val rightEyeCenter = 77
    const val rightEyeBottom = 76
    const val rightEyeLeftCorner = 58
    const val rightEyeRightCorner = 61

    const val eyeCenter = 43

    // 鼻子
    const val noseTop = 46
    const val noseLeft = 82
    const val noseRight = 83
    const val noseLowerMiddle = 49

    // 脸边沿
    const val leftCheekEdgeCenter = 4
    const val rightCheekEdgeCenter = 28

    // 嘴巴
    const val mouthLeftCorner = 84
    const val mouthRightCorner = 90
    const val mouthUpperLipTop = 87
    const val mouthUpperLipBottom = 98
    const val mouthLowerLipTop = 102
    const val mouthLowerLipBottom = 93

    // 下巴
    const val chinLeft = 14
    const val chinRight = 18
    const val chinCenter = 16

    // 扩展的关键点(8个)
    const val mouthCenter = 106
    const val leftEyebrowCenter = 107
    const val rightEyebrowCenter = 108
    const val leftHead = 109
    const val headCenter = 110
    const val rightHead = 111
    const val leftCheekCenter = 112
    const val rightCheekCenter = 113
}
