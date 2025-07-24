package com.cgfay.facedetect.engine

import com.cgfay.facedetect.listener.FaceTrackerCallback

/**
 * Builder for face tracker initialization.
 */
class FaceTrackerBuilder(private val mFaceTracker: FaceTracker, callback: FaceTrackerCallback) {

    private val mFaceTrackParam: FaceTrackParam = FaceTrackParam.getInstance().apply {
        trackerCallback = callback
    }

    /**
     * Prepare the tracker.
     */
    fun initTracker() {
        mFaceTracker.initTracker()
    }

    /**
     * Enable preview tracking.
     */
    fun previewTrack(previewTrack: Boolean): FaceTrackerBuilder {
        mFaceTrackParam.previewTrack = previewTrack
        return this
    }
}
