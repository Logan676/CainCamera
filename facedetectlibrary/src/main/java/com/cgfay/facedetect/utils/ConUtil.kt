package com.cgfay.facedetect.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.media.ExifInterface
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.text.TextUtils
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ConUtil {

    @JvmStatic
    fun isReadKey(context: Context): Boolean {
        var inputStream: InputStream? = null
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        try {
            inputStream = context.assets.open("key")
            var count: Int
            while (inputStream.read(buffer).also { count = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, count)
            }
            byteArrayOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val str = String(byteArrayOutputStream.toByteArray())
        var key: String? = null
        var screct: String? = null
        try {
            val strs = str.split(";")
            key = strs[0].trim { it <= ' ' }
            screct = strs[1].trim { it <= ' ' }
        } catch (e: Exception) {
        }
        FaceppConstraints.API_KEY = key
        FaceppConstraints.API_SECRET = screct
        return !(FaceppConstraints.API_KEY == null || FaceppConstraints.API_SECRET == null)
    }

    @JvmStatic
    fun toggleHideyBar(activity: Activity) {
        val uiOptions = activity.window.decorView.systemUiVisibility
        var newUiOptions = uiOptions
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 19) {
            newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        activity.window.decorView.systemUiVisibility = newUiOptions
    }

    @JvmStatic
    fun getFormatterDate(time: Long): String {
        val d = Date(time)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(d)
    }

    @JvmStatic
    fun getUUIDString(mContext: Context): String {
        val KEY_UUID = "key_uuid"
        val sharedUtil = SharedUtil(mContext)
        var uuid = sharedUtil.getStringValueByKey(KEY_UUID)
        if (uuid != null && uuid.trim { it <= ' ' }.isNotEmpty()) return uuid
        uuid = UUID.randomUUID().toString()
        uuid = Base64.encodeToString(uuid.toByteArray(), Base64.DEFAULT)
        sharedUtil.saveStringValue(KEY_UUID, uuid)
        return uuid
    }

    @JvmStatic
    fun decodeToBitMap(data: ByteArray, _camera: Camera): Bitmap? {
        val size = _camera.parameters.previewSize
        return try {
            val image = YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, size.width, size.height), 80, stream)
            val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()
            bmp
        } catch (ex: Exception) {
            null
        }
    }

    @JvmStatic
    fun isGoneKeyBoard(activity: Activity) {
        activity.currentFocus?.let {
            (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    @JvmStatic
    var wakeLock: PowerManager.WakeLock? = null

    @JvmStatic
    fun acquireWakeLock(context: Context) {
        if (wakeLock == null) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag")
            wakeLock?.acquire()
        }
    }

    @JvmStatic
    fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
            wakeLock = null
        }
    }

    @JvmStatic
    fun getGrayscale(bitmap: Bitmap?): ByteArray? {
        if (bitmap == null) return null
        val ret = ByteArray(bitmap.width * bitmap.height)
        for (j in 0 until bitmap.height) {
            for (i in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(i, j)
                val red = pixel and 0x00FF0000 shr 16
                val green = pixel and 0x0000FF00 shr 8
                val blue = pixel and 0x000000FF
                ret[j * bitmap.width + i] = ((299 * red + 587 * green + 114 * blue) / 1000).toByte()
            }
        }
        return ret
    }

    @JvmStatic
    fun convertYUV21FromRGB(bitmapSrc: Bitmap): ByteArray {
        var bitmap = rotaingImageView(90, bitmapSrc)
        val inputWidth = bitmap.width
        val inputHeight = bitmap.height
        val argb = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)
        bitmap.recycle()
        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val a = argb[index] and -0x1000000 shr 24
                val R = argb[index] and 0xff0000 shr 16
                val G = argb[index] and 0xff00 shr 8
                val B = argb[index] and 0xff
                var Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                var U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                var V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
                yuv420sp[yIndex++] = if (Y < 0) 0 else if (Y > 255) 255.toByte() else Y.toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = if (V < 0) 0 else if (V > 255) 255.toByte() else V.toByte()
                    yuv420sp[uvIndex++] = if (U < 0) 0 else if (U > 255) 255.toByte() else U.toByte()
                }
                index++
            }
        }
    }

    @JvmStatic
    fun getFileContent(context: Context, id: Int): ByteArray? {
        var inputStream: InputStream? = null
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        try {
            inputStream = context.resources.openRawResource(id)
            var count: Int
            while (inputStream.read(buffer).also { count = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, count)
            }
            byteArrayOutputStream.close()
        } catch (e: IOException) {
            return null
        } finally {
            inputStream = null
        }
        return byteArrayOutputStream.toByteArray()
    }

    @JvmStatic
    fun showToast(context: Context?, str: String) {
        if (context != null) {
            val toast = Toast.makeText(context, str, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 30)
            toast.show()
        }
    }

    @JvmStatic
    fun showLongToast(context: Context?, str: String) {
        if (context != null) {
            val toast = Toast.makeText(context, str, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP, 0, 30)
            toast.show()
        }
    }

    @JvmStatic
    fun getVersionName(context: Context): String? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    @JvmStatic
    fun convert(bitmap: Bitmap, mIsFrontalCamera: Boolean): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val newbBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cv = Canvas(newbBitmap)
        val m = Matrix()
        if (mIsFrontalCamera) {
            m.postScale(-1f, 1f)
        }
        val bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true)
        cv.drawBitmap(bitmap2, Rect(0, 0, bitmap2.width, bitmap2.height), Rect(0, 0, w, h), null)
        return newbBitmap
    }

    @JvmStatic
    fun readYUVInfo(ctx: Context): ByteArray? {
        val path = getDiskCachePath(ctx)
        val pathName = "$path/yuv.img"
        val file = File(pathName)
        if (!file.exists()) return null
        var inputStream: InputStream? = null
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        try {
            inputStream = FileInputStream(file)
            var count: Int
            while (inputStream.read(buffer).also { count = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, count)
            }
            byteArrayOutputStream.close()
        } catch (e: IOException) {
            return null
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return byteArrayOutputStream.toByteArray()
    }

    @JvmStatic
    fun saveYUVInfo(ctx: Context, arr: ByteArray?) {
        if (arr == null) return
        val path = getDiskCachePath(ctx)
        val pathName = "$path/yuv.img"
        val file = File(pathName)
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(arr)
            fileOutputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun saveBitmap(mContext: Context, bitmaptosave: Bitmap?): String? {
        if (bitmaptosave == null) return null
        val mediaStorageDir = mContext.getExternalFilesDir("megvii")
        if (mediaStorageDir != null && !mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        val bitmapFileName = System.currentTimeMillis().toString()
        var fos: FileOutputStream? = null
        return try {
            fos = FileOutputStream("${mediaStorageDir}/${bitmapFileName}")
            val successful = bitmaptosave.compress(Bitmap.CompressFormat.JPEG, 75, fos)
            if (successful) "${mediaStorageDir.absolutePath}/$bitmapFileName" else null
        } catch (e: FileNotFoundException) {
            null
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun revitionImage(path: String?, width: Int, height: Int): Bitmap? {
        if (path == null || TextUtils.isEmpty(path) || !File(path).exists()) return null
        var input: BufferedInputStream? = null
        return try {
            val degree = readPictureDegree(path)
            input = BufferedInputStream(FileInputStream(File(path)))
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(input, null, options)
            options.inSampleSize = calculateInSampleSize(options, width, height)
            input.close()
            input = BufferedInputStream(FileInputStream(File(path)))
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeStream(input, null, options)
            rotaingImageView(degree, bitmap)
        } catch (e: Exception) {
            null
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
        }
        return degree
    }

    @JvmStatic
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }

    @JvmStatic
    fun rotaingImageView(angle: Int, bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @JvmStatic
    fun getDiskCachePath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            context.externalCacheDir!!.path
        } else {
            context.cacheDir.path
        }
    }

    @JvmStatic
    fun getSDRootPath(): String? {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            Environment.getExternalStorageDirectory().path
        } else {
            null
        }
    }

    @JvmStatic
    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    @JvmStatic
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
}
