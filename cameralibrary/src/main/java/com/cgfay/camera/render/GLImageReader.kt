package com.cgfay.camera.render

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import android.opengl.EGLContext
import com.cgfay.filter.gles.EglCore
import com.cgfay.filter.gles.WindowSurface
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * Reads texture data from GPU using an offscreen surface.
 */
class GLImageReader(context: EGLContext, private val listener: ImageReceiveListener?) {

    private var windowSurface: WindowSurface? = null
    private var eglCore: EglCore? = EglCore(context, EglCore.FLAG_RECORDABLE)
    private var imageReader: ImageReader? = null
    private var imageFilter: GLImageFilter? = null
    private var vertexBuffer: FloatBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
    private var textureBuffer: FloatBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)

    fun init(width: Int, height: Int) {
        if (imageReader == null) {
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, MAX_IMAGE_NUMBER)
            imageReader?.setOnImageAvailableListener(ImageAvailable(), null)
            windowSurface = WindowSurface(eglCore, imageReader!!.surface, true)
        }
        if (imageFilter == null) {
            imageFilter = GLImageFilter(null)
            imageFilter?.onInputSizeChanged(width, height)
            imageFilter?.onDisplaySizeChanged(width, height)
        }
    }

    fun drawFrame(texture: Int) {
        windowSurface?.makeCurrent()
        imageFilter?.drawFrame(texture, vertexBuffer, textureBuffer)
        windowSurface?.swapBuffers()
    }

    fun release() {
        windowSurface?.makeCurrent()
        imageReader?.close()
        imageReader = null
        imageFilter?.release()
        imageFilter = null
        windowSurface?.release()
        windowSurface = null
        eglCore?.release()
        eglCore = null
    }

    private inner class ImageAvailable : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader) {
            val l = listener ?: return
            val image = reader.acquireNextImage()
            val planes = image.planes
            val width = image.width
            val height = image.height
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val data = ByteArray(rowStride * height)
            val buffer: ByteBuffer = planes[0].buffer
            buffer.get(data)
            val pixelData = IntArray(width * height)
            var offset = 0
            var index = 0
            for (i in 0 until height) {
                for (j in 0 until width) {
                    var pixel = 0
                    pixel = pixel or ((data[offset].toInt() and 0xff) shl 16)
                    pixel = pixel or ((data[offset + 1].toInt() and 0xff) shl 8)
                    pixel = pixel or (data[offset + 2].toInt() and 0xff)
                    pixel = pixel or ((data[offset + 3].toInt() and 0xff) shl 24)
                    pixelData[index++] = pixel
                    offset += pixelStride
                }
                offset += rowPadding
            }
            val bitmap = Bitmap.createBitmap(pixelData, width, height, Bitmap.Config.ARGB_8888)
            image.close()
            l.onImageReceive(bitmap)
        }
    }

    fun interface ImageReceiveListener {
        fun onImageReceive(bitmap: Bitmap)
    }

    companion object {
        private const val MAX_IMAGE_NUMBER = 1
    }
}
