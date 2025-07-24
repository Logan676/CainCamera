package com.cgfay.uitls.utils

import android.graphics.RectF

object RectUtils {
    fun scale(rectF: RectF, scale: Float) {
        val width = rectF.width()
        val height = rectF.height()
        val newWidth = scale * width
        val newHeight = scale * height
        val dx = (newWidth - width) / 2
        val dy = (newHeight - height) / 2
        rectF.left -= dx
        rectF.top -= dy
        rectF.right += dx
        rectF.bottom += dy
    }

    fun rotate(rect: RectF, centerX: Float, centerY: Float, rotateAngle: Float) {
        val x = rect.centerX()
        val y = rect.centerY()
        val sinA = Math.sin(Math.toRadians(rotateAngle.toDouble())).toFloat()
        val cosA = Math.cos(Math.toRadians(rotateAngle.toDouble())).toFloat()
        val newX = centerX + (x - centerX) * cosA - (y - centerY) * sinA
        val newY = centerY + (y - centerY) * cosA + (x - centerX) * sinA
        val dx = newX - x
        val dy = newY - y
        rect.offset(dx, dy)
    }
}
