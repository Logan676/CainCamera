package com.cgfay.caincamera.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.caincamera.R
import com.cgfay.uitls.utils.DensityUtils

/**
 * Kotlin version of VideoClipSeekBar with Compose support.
 */
class VideoClipSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object { private const val MAX = 100 }

    private var widthPx = 0
    private var heightPx = 0
    private lateinit var bar: RectF
    private lateinit var rectStart: RectF
    private lateinit var rectEnd: RectF
    private val paintBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.blue)
    }
    private val paintTouchBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.yellow)
    }
    private var startPosition = -1f
    private var endPosition = -1f
    private var tempX = 0f
    private val touchBarWidth = DensityUtils.dp2px(context, 15).toFloat()
    private val margin = DensityUtils.dp2px(context, 5).toFloat()
    private val roundCorner = DensityUtils.dp2px(context, 10).toFloat()
    private var moveStart = false
    private var moveEnd = false
    private var max = MAX

    private var callBack: OnCutBarChangeListener? = null

    fun addCutBarChangeListener(listener: OnCutBarChangeListener?) {
        callBack = listener
    }

    interface OnCutBarChangeListener {
        fun onStartProgressChanged(screenStartX: Float, progress: Int)
        fun onEndProgressChanged(screenEndX: Float, progress: Int)
        fun onTouchFinish(start: Int, end: Int)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthPx = measuredWidth
        heightPx = measuredHeight
        bar = RectF(0f, margin, widthPx.toFloat(), heightPx - margin)
        if (startPosition == -1f) startPosition = 0f
        if (endPosition == -1f) endPosition = widthPx - touchBarWidth
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                tempX = event.x
                moveStart = isTouchStartBar(tempX)
                moveEnd = !moveStart && isTouchEndBar(tempX)
            }
            MotionEvent.ACTION_MOVE -> {
                tempX = event.x
                if (moveStart) {
                    if (tempX + touchBarWidth < endPosition && tempX >= 0) {
                        startPosition = tempX
                        invalidate()
                        callBack?.onStartProgressChanged(
                            event.rawX,
                            ((startPosition / widthPx) * max).toInt()
                        )
                    }
                } else if (moveEnd) {
                    if (tempX - touchBarWidth > startPosition && tempX + touchBarWidth <= widthPx) {
                        endPosition = tempX
                        invalidate()
                        callBack?.onEndProgressChanged(
                            event.rawX,
                            (((endPosition + touchBarWidth) / widthPx) * max).toInt()
                        )
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                callBack?.onTouchFinish(
                    ((startPosition / widthPx) * max).toInt(),
                    (((endPosition + touchBarWidth) / widthPx) * max).toInt()
                )
                moveStart = false
                moveEnd = false
            }
        }
        return true
    }

    private fun isTouchStartBar(x: Float) = x >= startPosition && x <= startPosition + touchBarWidth
    private fun isTouchEndBar(x: Float) = x > endPosition && x <= endPosition + touchBarWidth

    fun getMax() = max

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBar(canvas)
        drawStart(canvas)
        drawEnd(canvas)
    }

    private fun drawStart(canvas: Canvas) {
        rectStart = RectF(startPosition, 0f, touchBarWidth + startPosition, heightPx.toFloat())
        canvas.drawRoundRect(rectStart, roundCorner, roundCorner, paintTouchBar)
    }

    private fun drawEnd(canvas: Canvas) {
        rectEnd = RectF(endPosition, 0f, touchBarWidth + endPosition, heightPx.toFloat())
        canvas.drawRoundRect(rectEnd, roundCorner, roundCorner, paintTouchBar)
    }

    private fun drawBar(canvas: Canvas) {
        canvas.drawRoundRect(bar, roundCorner, roundCorner, paintBar)
    }
}

/**
 * Compose wrapper for [VideoClipSeekBar].
 */
@Composable
fun VideoClipSeekBarWidget(
    modifier: Modifier = Modifier,
    max: Int = 100,
    listener: VideoClipSeekBar.OnCutBarChangeListener? = null,
    onCreated: (VideoClipSeekBar) -> Unit = {},
    update: (VideoClipSeekBar) -> Unit = {}
) {
    val context = LocalContext.current
    val view = remember(max, listener) {
        VideoClipSeekBar(context).apply {
            this.max = max
            addCutBarChangeListener(listener)
        }.also(onCreated)
    }
    AndroidView(modifier = modifier, factory = { view }, update = update)
}

