package com.cgfay.picker.fragment

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.picker.widget.subsamplingview.ImageSource
import com.cgfay.picker.widget.subsamplingview.OnImageEventListener
import com.cgfay.picker.widget.subsamplingview.SubsamplingScaleImageView
import com.cgfay.scan.R
import com.cgfay.uitls.utils.DisplayUtils

/**
 * Compose version of [MediaPreviewFragment].
 */
class MediaPreviewFragment : AppCompatDialogFragment() {

    private var originImageView: SubsamplingScaleImageView? = null
    private var videoView: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PickerPreviewStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent { MediaPreviewScreen() }
        }
    }

    @Composable
    private fun MediaPreviewScreen() {
        val mediaData = arguments?.getParcelable<MediaData>(CURRENT_MEDIA)
        if (mediaData == null) {
            LaunchedEffect(Unit) { removeFragment() }
            return
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (mediaData.isImage()) {
                val context = LocalContext.current
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        SubsamplingScaleImageView(ctx).apply {
                            originImageView = this
                            maxScale = MAX_SCALE
                            setOnClickListener { removeFragment() }
                            setOnImageEventListener(object : OnImageEventListener {
                                override fun onImageLoaded(width: Int, height: Int) {
                                    calculatePictureScale(this@apply, width, height)
                                }
                            })
                            setImage(ImageSource.uri(mediaData.contentUri))
                        }
                    }
                )
            } else {
                val context = LocalContext.current
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            videoView = this
                            setOnPreparedListener { seekTo(0) }
                            setOnCompletionListener { seekTo(0) }
                            setOnErrorListener { mp, what, extra ->
                                stopPlayback();
                                false
                            }
                            setVideoPath(MediaMetadataUtils.getPath(context, mediaData.contentUri))
                            setOnClickListener { removeFragment() }
                            start()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (videoView != null && !videoView!!.isPlaying) {
            videoView!!.start()
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView != null && videoView!!.canPause()) {
            videoView!!.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView?.stopPlayback()
    }

    private fun calculatePictureScale(view: SubsamplingScaleImageView, width: Int, height: Int) {
        if (height >= LONG_IMG_MINIMUM_LENGTH && height / width >= LONG_IMG_ASPECT_RATIO) {
            val screenWidth = DisplayUtils.getScreenWidth(context)
            val scale = screenWidth / width.toFloat()
            val centerX = screenWidth / 2f
            view.setScaleAndCenterWithAnim(scale, PointF(centerX, 0f))
            view.setDoubleTapZoomScale(scale)
        }
    }

    private fun removeFragment() {
        parentFragment?.childFragmentManager?.beginTransaction()?.remove(this)
            ?.commitNowAllowingStateLoss()
            ?: activity?.supportFragmentManager?.beginTransaction()?.remove(this)
                ?.commitNowAllowingStateLoss()
    }

    companion object {
        private const val CURRENT_MEDIA = "current_media"
        private const val MAX_SCALE = 15f
        private const val LONG_IMG_ASPECT_RATIO = 3
        private const val LONG_IMG_MINIMUM_LENGTH = 1500

        fun newInstance(mediaData: MediaData): MediaPreviewFragment {
            val fragment = MediaPreviewFragment()
            val bundle = Bundle()
            bundle.putParcelable(CURRENT_MEDIA, mediaData)
            fragment.arguments = bundle
            return fragment
        }
    }
}
