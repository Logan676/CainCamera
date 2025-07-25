package com.cgfay.filter.glfilter.stickers

import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.cgfay.filter.glfilter.resource.ResourceCodec
import com.cgfay.filter.glfilter.resource.ResourceIndexCodec
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.landmark.LandmarkEngine
import com.cgfay.uitls.utils.BitmapUtils
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * 动态贴纸加载器
 */
class DynamicStickerLoader @JvmOverloads constructor(
    var isStaticSticker: Boolean = false,
    filter: DynamicStickerBaseFilter,
    private val stickerData: DynamicStickerData,
    folderPath: String
) {

    companion object {
        private const val TAG = "DynamicStickerLoader"
    }

    // 贴纸纹理
    private var stickerTexture = OpenGLUtils.GL_NOT_TEXTURE
    // 暂存纹理id，用于复用
    private var restoreTexture = OpenGLUtils.GL_NOT_TEXTURE
    // 贴纸所在的文件夹
    private val folderPath: String = if (folderPath.startsWith("file://")) {
        folderPath.substring("file://".length)
    } else {
        folderPath
    }
    // 索引索引加载器
    private var resourceIndexCodec: ResourceIndexCodec? = null
    // 当前索引
    private var frameIndex = -1
    // 当前时间
    private var currentTime = -1L
    // 贴纸滤镜
    private val weakFilter = WeakReference(filter)

    init {
        ResourceCodec.getResourceFile(this.folderPath)?.let { pair ->
            resourceIndexCodec = ResourceIndexCodec(
                "${this.folderPath}/${pair.first}",
                "${this.folderPath}/${pair.second}"
            )
        }
        resourceIndexCodec?.let {
            try {
                it.init()
            } catch (e: IOException) {
                Log.e(TAG, "init merge res reader failed", e)
                resourceIndexCodec = null
            }
        }

        if (!TextUtils.isEmpty(stickerData.audioPath)) {
            val str = if (folderPath.startsWith("file://")) {
                folderPath.substring("file://".length)
            } else {
                folderPath
            }
            weakFilter.get()?.setAudioPath(Uri.parse("$str/${stickerData.audioPath}"))
            weakFilter.get()?.setLooping(stickerData.audioLooping)
        }
    }

    /** 更新贴纸纹理 */
    fun updateStickerTexture() {
        if (!LandmarkEngine.getInstance().hasFace() && !isStaticSticker) {
            currentTime = -1L
            weakFilter.get()?.stopPlayer()
            return
        }
        if (!TextUtils.isEmpty(stickerData.audioPath) && stickerData.action == 0) {
            weakFilter.get()?.startPlayer()
        }
        if (currentTime == -1L) {
            currentTime = System.currentTimeMillis()
        }
        var idx = ((System.currentTimeMillis() - currentTime) / stickerData.duration).toInt()
        if (idx >= stickerData.frames) {
            if (!stickerData.stickerLooping) {
                currentTime = -1L
                restoreTexture = stickerTexture
                stickerTexture = OpenGLUtils.GL_NOT_TEXTURE
                frameIndex = -1
                return
            }
            idx = 0
            currentTime = System.currentTimeMillis()
        }
        if (idx < 0) idx = 0
        if (frameIndex == idx) return

        if (idx == 0 && stickerData.audioLooping) {
            weakFilter.get()?.restartPlayer()
        }
        var bitmap: Bitmap? = resourceIndexCodec?.loadResource(idx)
        if (bitmap == null) {
            val path = String.format("${stickerData.stickerName}_%03d.png", idx)
            bitmap = BitmapUtils.getBitmapFromFile("$folderPath/$path")
        }
        if (bitmap != null) {
            if (stickerTexture == OpenGLUtils.GL_NOT_TEXTURE && restoreTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                stickerTexture = restoreTexture
            }
            stickerTexture = if (stickerTexture == OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.createTexture(bitmap)
            } else {
                OpenGLUtils.createTexture(bitmap, stickerTexture)
            }
            restoreTexture = stickerTexture
            frameIndex = idx
            bitmap.recycle()
        } else {
            restoreTexture = stickerTexture
            stickerTexture = OpenGLUtils.GL_NOT_TEXTURE
            frameIndex = -1
        }
    }

    /** 释放资源 */
    fun release() {
        if (stickerTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            stickerTexture = restoreTexture
        }
        OpenGLUtils.deleteTexture(stickerTexture)
        stickerTexture = OpenGLUtils.GL_NOT_TEXTURE
        restoreTexture = OpenGLUtils.GL_NOT_TEXTURE
        weakFilter.clear()
    }

    fun getStickerTexture(): Int = stickerTexture
    fun getMaxCount(): Int = stickerData.maxCount
    fun getStickerData(): DynamicStickerData = stickerData
}
