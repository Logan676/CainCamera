package com.cgfay.uitls.utils

import android.content.ContentValues
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import java.io.*
import java.nio.ByteBuffer

object BitmapUtils {
    private const val TAG = "BitmapUtils"

    /** 从普通文件中读入图片 */
    @JvmStatic
    fun getBitmapFromFile(fileName: String): Bitmap? {
        val file = File(fileName)
        if (!file.exists()) {
            return null
        }
        return try {
            BitmapFactory.decodeFile(fileName)
        } catch (e: Exception) {
            Log.e(TAG, "getBitmapFromFile: ", e)
            null
        }
    }

    /** 加载Assets文件夹下的图片 */
    @JvmStatic
    fun getImageFromAssetsFile(context: Context, fileName: String): Bitmap? {
        var bitmap: Bitmap? = null
        val manager: AssetManager = context.resources.assets
        try {
            val `is` = manager.open(fileName)
            bitmap = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    /** 加载Assets文件夹下的图片，支持复用 */
    @JvmStatic
    fun getImageFromAssetsFile(context: Context, fileName: String, inBitmap: Bitmap?): Bitmap? {
        var bitmap: Bitmap? = null
        val manager: AssetManager = context.resources.assets
        try {
            val `is` = manager.open(fileName)
            bitmap = if (inBitmap != null && !inBitmap.isRecycled) {
                val options = BitmapFactory.Options()
                options.inSampleSize = 1
                options.inMutable = true
                options.inBitmap = inBitmap
                BitmapFactory.decodeStream(`is`, null, options)
            } else {
                BitmapFactory.decodeStream(`is`)
            }
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    /** 计算 inSampleSize的值 */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
            var totalPixels = width * height / inSampleSize
            val totalReqPixelsCap = reqWidth * reqHeight * 2
            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2
                totalPixels /= 2
            }
        }
        return inSampleSize
    }

    /** 从文件读取Bitmap */
    @JvmStatic
    fun getBitmapFromFile(dst: File?, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (dst != null && dst.exists()) {
            var opts: BitmapFactory.Options? = null
            if (maxWidth > 0 && maxHeight > 0) {
                opts = BitmapFactory.Options()
                opts.inJustDecodeBounds = true
                BitmapFactory.decodeFile(dst.path, opts)
                opts.inSampleSize = calculateInSampleSize(opts, maxWidth, maxHeight)
                opts.inJustDecodeBounds = false
                opts.inInputShareable = true
                opts.inPurgeable = true
            }
            return try {
                BitmapFactory.decodeFile(dst.path, opts)
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                null
            }
        }
        return null
    }

    /** 从文件读取Bitmap，可选择处理方向 */
    @JvmStatic
    fun getBitmapFromFile(dst: File?, maxWidth: Int, maxHeight: Int, processOrientation: Boolean): Bitmap? {
        if (dst != null && dst.exists()) {
            var opts: BitmapFactory.Options? = null
            if (maxWidth > 0 && maxHeight > 0) {
                opts = BitmapFactory.Options()
                opts.inJustDecodeBounds = true
                BitmapFactory.decodeFile(dst.path, opts)
                opts.inSampleSize = calculateInSampleSize(opts, maxWidth, maxHeight)
                opts.inJustDecodeBounds = false
                opts.inInputShareable = true
                opts.inPurgeable = true
            }
            try {
                val bitmap = BitmapFactory.decodeFile(dst.path, opts)
                if (!processOrientation) {
                    return bitmap
                }
                val orientation = getOrientation(dst.path)
                if (orientation == 0) {
                    return bitmap
                } else {
                    val matrix = Matrix()
                    matrix.postRotate(orientation.toFloat())
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
            }
        }
        return null
    }

    /** 保存图片 */
    @JvmStatic
    fun saveBitmap(context: Context, path: String, bitmap: Bitmap) {
        saveBitmap(context, path, bitmap, true)
    }

    /** 保存图片 */
    @JvmStatic
    fun saveBitmap(context: Context, path: String, bitmap: Bitmap, addToMediaStore: Boolean) {
        val file = File(path)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        var fOut: FileOutputStream? = null
        try {
            fOut = FileOutputStream(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var compress = true
        if (path.endsWith(".png")) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        } else if (path.endsWith(".jpeg") || path.endsWith(".jpg")) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
        } else {
            compress = false
        }
        try {
            fOut?.flush()
            fOut?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (addToMediaStore && compress) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, path)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    /** 保存图片 */
    @JvmStatic
    fun saveBitmap(filePath: String, buffer: ByteBuffer, width: Int, height: Int) {
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(filePath))
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            bitmap = rotateBitmap(bitmap, 180, true)!!
            bitmap = flipBitmap(bitmap, true)!!
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bitmap.recycle()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
            } catch (e: IOException) {
            }
        }
    }

    /** 保存图片 */
    @JvmStatic
    fun saveBitmap(filePath: String, bitmap: Bitmap?) {
        if (bitmap == null) {
            return
        }
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(filePath))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bitmap.recycle()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
            } catch (e: IOException) {
            }
        }
    }

    /** 将Bitmap图片旋转90度 */
    @JvmStatic
    fun rotateBitmap(data: ByteArray): Bitmap? {
        return rotateBitmap(data, 90)
    }

    /** 将Bitmap图片旋转一定角度 */
    @JvmStatic
    fun rotateBitmap(data: ByteArray, rotate: Int): Bitmap? {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val matrix = Matrix()
        matrix.reset()
        matrix.postRotate(rotate.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        System.gc()
        return rotatedBitmap
    }

    /** 将Bitmap图片旋转90度 */
    @JvmStatic
    fun rotateBitmap(bitmap: Bitmap?, isRecycled: Boolean): Bitmap? {
        return rotateBitmap(bitmap, 90, isRecycled)
    }

    /** 将Bitmap图片旋转一定角度 */
    @JvmStatic
    fun rotateBitmap(bitmap: Bitmap?, rotate: Int, isRecycled: Boolean): Bitmap? {
        if (bitmap == null) {
            return null
        }
        val matrix = Matrix()
        matrix.reset()
        matrix.postRotate(rotate.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (!bitmap.isRecycled && isRecycled) {
            bitmap.recycle()
        }
        return rotatedBitmap
    }

    /** 镜像翻转图片 */
    @JvmStatic
    fun flipBitmap(bitmap: Bitmap?, isRecycled: Boolean): Bitmap? {
        return flipBitmap(bitmap, true, false, isRecycled)
    }

    /** 翻转图片 */
    @JvmStatic
    fun flipBitmap(bitmap: Bitmap?, flipX: Boolean, flipY: Boolean, isRecycled: Boolean): Bitmap? {
        if (bitmap == null) {
            return null
        }
        val matrix = Matrix()
        matrix.setScale(if (flipX) -1f else 1f, if (flipY) -1f else 1f)
        matrix.postTranslate(bitmap.width.toFloat(), 0f)
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
        if (isRecycled && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        return result
    }

    /** 获取图片旋转角度 */
    private fun getOrientation(path: String): Int {
        var rotation = 0
        try {
            val exif = androidx.exifinterface.media.ExifInterface(path)
            val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)
            rotation = when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rotation
    }
}
