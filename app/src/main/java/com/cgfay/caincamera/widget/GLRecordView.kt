package com.cgfay.caincamera.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.GestureDetectorCompat
import com.cgfay.caincamera.R
import com.cgfay.uitls.utils.DensityUtils

/**
 * OpenGL recording view written in Kotlin with Compose support.
 */
class GLRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private var touchX = 0f
    private var touchY = 0f
    private var focusAnimator: ValueAnimator? = null
    private var focusImageView: ImageView? = null
    private var scroller: OnTouchScroller? = null
    private var multiClickListener: OnMultiClickListener? = null

    private val gestureDetector =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                touchX = e.x
                touchY = e.y
                multiClickListener?.onSurfaceSingleClick(e.x, e.y)
                showFocusAnimation()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                multiClickListener?.onSurfaceDoubleClick(e.x, e.y)
                return true
            }

            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (kotlin.math.abs(distanceX) < kotlin.math.abs(distanceY) * 1.5f) {
                    val leftScroll = e1.x < width / 2f
                    if (distanceY > 0) {
                        scroller?.swipeUpper(leftScroll, kotlin.math.abs(distanceY))
                    } else {
                        scroller?.swipeDown(leftScroll, kotlin.math.abs(distanceY))
                    }
                }
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (kotlin.math.abs(velocityX) > kotlin.math.abs(velocityY) * 1.5f) {
                    if (velocityX < 0) {
                        scroller?.swipeBack()
                    } else {
                        scroller?.swipeFrontal()
                    }
                }
                return false
            }
        })

    init {
        isClickable = true
        if (Build.VERSION.SDK_INT >= 21) {
            outlineProvider = RoundOutlineProvider(
                DensityUtils.dp2px(context, 7.5f).toFloat()
            )
            clipToOutline = true
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    /**
     * Show focus animation when single tapped.
     */
    fun showFocusAnimation() {
        if (focusAnimator == null) {
            focusImageView = ImageView(context).apply {
                setImageResource(R.drawable.video_focus)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                measure(0, 0)
                x = touchX - measuredWidth / 2f
                y = touchY - measuredHeight / 2f
            }
            val parent = parent as ViewGroup
            parent.addView(focusImageView)
            focusAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 500
                addUpdateListener { animator ->
                    val value = animator.animatedValue as Float
                    focusImageView?.scaleX = 2 - value
                    focusImageView?.scaleY = 2 - value
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        focusImageView?.let { parent.removeView(it) }
                        focusAnimator = null
                    }
                })
                start()
            }
        }
    }

    fun addOnTouchScroller(scroller: OnTouchScroller?) {
        this.scroller = scroller
    }

    fun addMultiClickListener(listener: OnMultiClickListener?) {
        this.multiClickListener = listener
    }

    private class RoundOutlineProvider(private val radius: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val selfRect = Rect(0, 0, rect.right - rect.left, rect.bottom - rect.top)
            outline.setRoundRect(selfRect, radius)
        }
    }

    interface OnTouchScroller {
        fun swipeBack()
        fun swipeFrontal()
        fun swipeUpper(startInLeft: Boolean, distance: Float)
        fun swipeDown(startInLeft: Boolean, distance: Float)
    }

    interface OnMultiClickListener {
        fun onSurfaceSingleClick(x: Float, y: Float)
        fun onSurfaceDoubleClick(x: Float, y: Float)
    }
}

/**
 * Compose wrapper for [GLRecordView].
 */
@Composable
fun GLRecordViewWidget(
    modifier: Modifier = Modifier,
    scroller: GLRecordView.OnTouchScroller? = null,
    multiClickListener: GLRecordView.OnMultiClickListener? = null,
    onCreated: (GLRecordView) -> Unit = {},
    update: (GLRecordView) -> Unit = {}
) {
    val context = LocalContext.current
    val view = remember(scroller, multiClickListener) {
        GLRecordView(context).apply {
            addOnTouchScroller(scroller)
            addMultiClickListener(multiClickListener)
        }.also(onCreated)
    }
    AndroidView(modifier = modifier, factory = { view }, update = update)
}

