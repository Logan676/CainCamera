package com.cgfay.filter.glfilter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLUtils
import android.opengl.Matrix
import android.text.TextUtils
import android.util.Log
import com.cgfay.uitls.utils.BitmapUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL11

/**
 * Collection of common OpenGL helper methods.
 */
object OpenGLUtils {
    const val TAG = "OpenGLUtils"
    const val GL_NOT_INIT = -1
    const val GL_NOT_TEXTURE = -1

    /** Identity matrix used by some filters. */
    val IDENTITY_MATRIX: FloatArray = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

    private const val SIZEOF_FLOAT = 4
    private const val SIZEOF_SHORT = 2

    /** Read shader source from a file path. */
    @JvmStatic
    fun getShaderFromFile(filePath: String?): String? {
        if (filePath.isNullOrEmpty()) return null
        val file = File(filePath)
        if (file.isDirectory) return null
        val inputStream: InputStream? = try {
            FileInputStream(file)
        } catch (e: IOException) {
            e.printStackTrace(); null
        }
        return getShaderStringFromStream(inputStream)
    }

    /** Read shader source from assets. */
    @JvmStatic
    fun getShaderFromAssets(context: Context, path: String): String? {
        val inputStream: InputStream? = try {
            context.resources.assets.open(path)
        } catch (e: IOException) {
            e.printStackTrace(); null
        }
        return getShaderStringFromStream(inputStream)
    }

    private fun getShaderStringFromStream(inputStream: InputStream?): String? {
        if (inputStream == null) return null
        return try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val builder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                builder.append(line).append('\n')
            }
            reader.close()
            builder.toString()
        } catch (e: IOException) {
            e.printStackTrace(); null
        }
    }

    /** Create program from vertex and fragment source. */
    @JvmStatic
    @Synchronized
    fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) return 0
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragmentShader == 0) return 0
        var program = GLES30.glCreateProgram()
        checkGlError("glCreateProgram")
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES30.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
        GLES30.glAttachShader(program, fragmentShader)
        checkGlError("glAttachShader")
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "Could not link program:")
            Log.e(TAG, GLES30.glGetProgramInfoLog(program))
            GLES30.glDeleteProgram(program)
            program = 0
        }
        if (vertexShader > 0) {
            GLES30.glDetachShader(program, vertexShader)
            GLES30.glDeleteShader(vertexShader)
        }
        if (fragmentShader > 0) {
            GLES30.glDetachShader(program, fragmentShader)
            GLES30.glDeleteShader(fragmentShader)
        }
        return program
    }

    /** Compile shader code. */
    @JvmStatic
    fun loadShader(shaderType: Int, source: String): Int {
        val shader = GLES30.glCreateShader(shaderType)
        checkGlError("glCreateShader type=$shaderType")
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + GLES30.glGetShaderInfoLog(shader))
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    /** Check for OpenGL errors. */
    @JvmStatic
    fun checkGlError(op: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            val msg = "$op: glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
        }
    }

    /** Create a FloatBuffer from float array. */
    @JvmStatic
    fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

    /** Create a FloatBuffer from Float list. */
    @JvmStatic
    fun createFloatBuffer(data: List<Float>): FloatBuffer {
        val coords = FloatArray(data.size)
        for (i in coords.indices) {
            coords[i] = data[i]
        }
        return createFloatBuffer(coords)
    }

    /** Create a ShortBuffer from short array. */
    @JvmStatic
    fun createShortBuffer(coords: ShortArray): ShortBuffer {
        val bb = ByteBuffer.allocateDirect(coords.size * SIZEOF_SHORT)
        bb.order(ByteOrder.nativeOrder())
        val sb = bb.asShortBuffer()
        sb.put(coords)
        sb.position(0)
        return sb
    }

    /** Create a ShortBuffer from Short list. */
    @JvmStatic
    fun createShortBuffer(data: List<Short>): ShortBuffer {
        val coords = ShortArray(data.size)
        for (i in coords.indices) {
            coords[i] = data[i]
        }
        return createShortBuffer(coords)
    }

    /** Create framebuffer and texture for sampler2D. */
    @JvmStatic
    fun createFrameBuffer(frameBuffer: IntArray, frameBufferTexture: IntArray, width: Int, height: Int) {
        GLES30.glGenFramebuffers(frameBuffer.size, frameBuffer, 0)
        GLES30.glGenTextures(frameBufferTexture.size, frameBufferTexture, 0)
        for (i in frameBufferTexture.indices) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, frameBufferTexture[i])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA,
                width,
                height,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null
            )
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[i])
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER,
                GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D,
                frameBufferTexture[i],
                0
            )
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        }
        checkGlError("createFrameBuffer")
    }

    /** Create a standard texture of given type. */
    @JvmStatic
    fun createTexture(textureType: Int): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        checkGlError("glGenTextures")
        val textureId = textures[0]
        GLES30.glBindTexture(textureType, textureId)
        checkGlError("glBindTexture $textureId")
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexParameterf(textureType, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        checkGlError("glTexParameter")
        return textureId
    }

    /** Create texture from bitmap. */
    @JvmStatic
    fun createTexture(bitmap: Bitmap?): Int {
        val texture = IntArray(1)
        if (bitmap != null && !bitmap.isRecycled) {
            GLES30.glGenTextures(1, texture, 0)
            checkGlError("glGenTexture")
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            return texture[0]
        }
        return 0
    }

    /** Update or create texture with a bitmap. */
    @JvmStatic
    fun createTexture(bitmap: Bitmap?, texture: Int): Int {
        val result = IntArray(1)
        if (texture == GL_NOT_TEXTURE) {
            result[0] = createTexture(bitmap)
        } else {
            result[0] = texture
            if (bitmap != null && !bitmap.isRecycled) {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, result[0])
                GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, bitmap)
            }
        }
        return result[0]
    }

    /** Create texture from RGBA byte array. */
    @JvmStatic
    fun createTexture(bytes: ByteArray, width: Int, height: Int): Int =
        createTexture(bytes, width, height, GL_NOT_TEXTURE)

    /** Create texture from RGBA byte array with existing texture. */
    @JvmStatic
    fun createTexture(bytes: ByteArray, width: Int, height: Int, texture: Int): Int {
        require(bytes.size == width * height * 4) { "Illegal byte array" }
        return createTexture(ByteBuffer.wrap(bytes), width, height, texture)
    }

    /** Create texture from ByteBuffer. */
    @JvmStatic
    fun createTexture(byteBuffer: ByteBuffer, width: Int, height: Int): Int {
        require(byteBuffer.array().size == width * height * 4) { "Illegal byte array" }
        val texture = IntArray(1)
        GLES30.glGenTextures(1, texture, 0)
        if (texture[0] == 0) {
            Log.d(TAG, "Failed at glGenTextures")
            return 0
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0])
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA,
            width,
            height,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            byteBuffer
        )
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return texture[0]
    }

    /** Update existing texture with ByteBuffer. */
    @JvmStatic
    fun createTexture(byteBuffer: ByteBuffer, width: Int, height: Int, texture: Int): Int {
        require(byteBuffer.array().size == width * height * 4) { "Illegal byte array" }
        if (texture == GL_NOT_TEXTURE) {
            return createTexture(byteBuffer, width, height)
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexSubImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            0,
            0,
            width,
            height,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            byteBuffer
        )
        return texture
    }

    /** Create texture from an absolute file path. */
    @JvmStatic
    fun createTexture(filePath: String?): Int {
        val textureHandle = IntArray(1)
        textureHandle[0] = GL_NOT_TEXTURE
        if (filePath.isNullOrEmpty()) return GL_NOT_TEXTURE
        GLES30.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val bitmap = BitmapFactory.decodeFile(filePath)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0])
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        Log.d("createTextureFromAssets", "filePath:$filePath, texture = ${textureHandle[0]}")
        return textureHandle[0]
    }

    /** Create texture from asset image. */
    @JvmStatic
    fun createTextureFromAssets(context: Context, name: String): Int {
        val textureHandle = IntArray(1)
        GLES30.glGenTextures(1, textureHandle, 0)
        if (textureHandle[0] != 0) {
            val bitmap = BitmapUtils.getImageFromAssetsFile(context, name)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0])
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }
        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }
        return textureHandle[0]
    }

    /** Create an OES external texture. */
    @JvmStatic
    fun createOESTexture(): Int = createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)

    /** Delete a texture. */
    @JvmStatic
    fun deleteTexture(texture: Int) {
        val textures = IntArray(1)
        textures[0] = texture
        GLES30.glDeleteTextures(1, textures, 0)
    }

    /** Bind a 2D texture to a given index. */
    @JvmStatic
    fun bindTexture(location: Int, texture: Int, index: Int) {
        bindTexture(location, texture, index, GLES30.GL_TEXTURE_2D)
    }

    /** Bind a texture to a given index and type. */
    @JvmStatic
    fun bindTexture(location: Int, texture: Int, index: Int, textureType: Int) {
        require(index <= 31) { "index must be no more than 31!" }
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + index)
        GLES30.glBindTexture(textureType, texture)
        GLES30.glUniform1i(location, index)
    }

    /** Get human readable EGL error string. */
    @JvmStatic
    fun getErrorString(error: Int): String = when (error) {
        EGL10.EGL_SUCCESS -> "EGL_SUCCESS"
        EGL10.EGL_NOT_INITIALIZED -> "EGL_NOT_INITIALIZED"
        EGL10.EGL_BAD_ACCESS -> "EGL_BAD_ACCESS"
        EGL10.EGL_BAD_ALLOC -> "EGL_BAD_ALLOC"
        EGL10.EGL_BAD_ATTRIBUTE -> "EGL_BAD_ATTRIBUTE"
        EGL10.EGL_BAD_CONFIG -> "EGL_BAD_CONFIG"
        EGL10.EGL_BAD_CONTEXT -> "EGL_BAD_CONTEXT"
        EGL10.EGL_BAD_CURRENT_SURFACE -> "EGL_BAD_CURRENT_SURFACE"
        EGL10.EGL_BAD_DISPLAY -> "EGL_BAD_DISPLAY"
        EGL10.EGL_BAD_MATCH -> "EGL_BAD_MATCH"
        EGL10.EGL_BAD_NATIVE_PIXMAP -> "EGL_BAD_NATIVE_PIXMAP"
        EGL10.EGL_BAD_NATIVE_WINDOW -> "EGL_BAD_NATIVE_WINDOW"
        EGL10.EGL_BAD_PARAMETER -> "EGL_BAD_PARAMETER"
        EGL10.EGL_BAD_SURFACE -> "EGL_BAD_SURFACE"
        EGL11.EGL_CONTEXT_LOST -> "EGL_CONTEXT_LOST"
        else -> getHex(error)
    }

    private fun getHex(value: Int) = "0x" + Integer.toHexString(value)
}
