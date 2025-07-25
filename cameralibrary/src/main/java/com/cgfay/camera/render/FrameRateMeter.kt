package com.cgfay.camera.render

/**
 * Utility class to measure rendering FPS.
 */
class FrameRateMeter {
    private var times = 0
    private var currentFps = 0f
    private var updateTime = 0L

    /**
     * Update frame counter.
     */
    fun drawFrameCount() {
        val currentTime = System.currentTimeMillis()
        if (updateTime == 0L) {
            updateTime = currentTime
        }
        if (currentTime - updateTime > TIME_TRAVEL_MS) {
            currentFps = times.toFloat() / (currentTime - updateTime) * 1000f
            updateTime = currentTime
            times = 0
        }
        times++
    }

    /**
     * Current FPS value.
     */
    val fps: Float
        get() = if (System.currentTimeMillis() - updateTime > TIME_TRAVEL_MAX_DIVIDE) 0f else currentFps

    companion object {
        private const val TIME_TRAVEL = 1L
        private const val TIME_TRAVEL_MS = TIME_TRAVEL * 1000
        private const val TIME_TRAVEL_MAX_DIVIDE = 2 * TIME_TRAVEL_MS
    }
}
