package com.cgfay.image.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable

@SuppressLint("AppCompatCustomView")
class CustomImageView @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null
) : View(context, attrs) {

    private val mMatrix = Matrix()
    private val mPerspectMatrix = Matrix()

    private var mBitmap: Bitmap? = null

    fun setBitmap(bitmap: Bitmap) {
        mBitmap = bitmap
    }

    fun setPerspective(x: Int, y: Int, horizontal: Boolean) {
        // TODO: implement perspective transform
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mBitmap?.let {
            canvas.drawBitmap(it, mMatrix, null)
        }
    }
}
