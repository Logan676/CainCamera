package com.cgfay.image.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt

/**
 * Canvas based graffiti drawing view converted to Kotlin.
 */
class GraffitiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var paint: Paint = newPaint(Color.WHITE)
    private var path = Path()
    private val savePathList = mutableListOf<Path>()
    private val paintList = mutableListOf<Paint>()
    private var isDrawMode = false
    private var lineChangeListener: OnLineChangeListener? = null
    private var touchMode = false
    private var touchListener: OnTouchListener? = null
    private var color: Int = Color.WHITE

    fun setNewPaintColor(@ColorInt color: Int) {
        paint.color = color
        this.color = color
    }

    fun setNewPaintSize(size: Float) {
        paint.strokeWidth = size
    }

    fun newPaint(color: Int): Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = resources.getDimension(com.cgfay.utilslibrary.R.dimen.dp3)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        this.color = color
    }

    fun setDrawMode(flag: Boolean) {
        isDrawMode = flag
    }

    fun backPath() {
        if (savePathList.isNotEmpty()) {
            if (savePathList.size == 1) {
                path.reset()
                savePathList.clear()
                paintList.clear()
            } else {
                savePathList.removeLast()
                paintList.removeLast()
                path = savePathList.last()
                paint = paintList.last()
            }
            lineChangeListener?.onDeleteLine(savePathList.size)
        }
        invalidate()
    }

    fun clearPath() {
        path.reset()
        savePathList.clear()
        paintList.clear()
    }

    interface OnTouchListener {
        fun onDown()
        fun onUp()
    }

    interface OnLineChangeListener {
        fun onDrawLine(sum: Int)
        fun onDeleteLine(sum: Int)
    }

    fun setOnLineChangeListener(listener: OnLineChangeListener?) {
        lineChangeListener = listener
    }

    fun setOnTouchListener(listener: OnTouchListener?) {
        touchListener = listener
    }

    private var mX = 0f
    private var mY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDrawMode) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchMode = true
                    touchDown(event)
                    touchListener?.onDown()
                }
                MotionEvent.ACTION_MOVE -> touchMove(event)
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    touchMode = false
                    savePathList.add(Path(path))
                    paintList.add(Paint(paint))
                    touchListener?.onUp()
                    lineChangeListener?.onDrawLine(savePathList.size)
                }
            }
            invalidate()
        }
        return isDrawMode
    }

    private fun touchDown(event: MotionEvent) {
        path = Path()
        val x = event.x
        val y = event.y
        mX = x
        mY = y
        path.moveTo(x, y)
    }

    private fun touchMove(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= 3 || dy >= 3) {
            path.lineTo(x, y)
            mX = x
            mY = y
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in savePathList.indices) {
            canvas.drawPath(savePathList[i], paintList[i])
        }
        if (touchMode) canvas.drawPath(path, paint)
    }
}
