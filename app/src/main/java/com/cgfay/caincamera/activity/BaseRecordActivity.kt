package com.cgfay.caincamera.activity

import android.graphics.SurfaceTexture
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.NonNull

/**
 * Base activity for record screens using Jetpack Compose.
 */
abstract class BaseRecordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * \u663e\u793a\u63a7\u4ef6
     */
    abstract fun showViews()

    /**
     * \u9690\u85cf\u63a7\u4ef6
     */
    abstract fun hideViews()

    /**
     * \u8bbe\u7f6e\u5f55\u5236\u8fdb\u5ea6
     */
    abstract fun setRecordProgress(progress: Float)

    /**
     * \u52a0\u5165\u4e00\u6bb5\u89c6\u9891
     */
    abstract fun addProgressSegment(progress: Float)

    /**
     * \u5220\u9664\u4e00\u6bb5\u89c6\u9891
     */
    abstract fun deleteProgressSegment()

    /**
     * \u7ed1\u5b9aSurfaceTexture
     */
    abstract fun bindSurfaceTexture(@NonNull surfaceTexture: SurfaceTexture)

    /**
     * \u5e27\u53ef\u7528\u5237\u65b0
     */
    abstract fun onFrameAvailable()

    /**
     * \u66f4\u65b0\u7eb9\u7406\u5927\u5c0f
     */
    abstract fun updateTextureSize(width: Int, height: Int)

    /**
     * \u663e\u793a\u5bf9\u8bdd\u6846
     */
    abstract fun showProgressDialog()

    /**
     * \u9690\u85cf\u5bf9\u8bdd\u6846
     */
    abstract fun hideProgressDialog()

    /**
     * \u663e\u793aToast\u63d0\u793a
     */
    abstract fun showToast(tips: String)
}
