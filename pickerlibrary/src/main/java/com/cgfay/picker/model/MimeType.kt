package com.cgfay.picker.model

import androidx.collection.ArraySet
import java.util.Arrays
import java.util.EnumSet
import java.util.Set

/**
 * 项目支持的媒体类型
 */
enum class MimeType(val mimeType: String, val extensions: Set<String>) {
    // 图片类型
    JPEG("image/jpeg", arraySetOf("jpeg")),
    JPG("image/jpg", arraySetOf("jpg")),
    BMP("image/bmp", arraySetOf("bmp")),
    PNG("image/png", arraySetOf("png")),
    GIF("image/gif", arraySetOf("gif")),
    // 视频类型
    MPEG("video/mpeg", arraySetOf("mpeg", "mpg")),
    MP4("video/mp4", arraySetOf("mp4", "m4v")),
    GPP("video/3gpp", arraySetOf("3gpp")),
    MKV("video/x-matroska", arraySetOf("mkv")),
    AVI("video/avi", arraySetOf("avi"));

    companion object {
        @JvmStatic
        fun ofAll(): Set<MimeType> = EnumSet.allOf(MimeType::class.java)

        @JvmStatic
        fun of(type: MimeType, vararg rest: MimeType): Set<MimeType> = EnumSet.of(type, *rest)

        // 图片
        @JvmStatic
        fun ofImage(): Set<MimeType> = EnumSet.of(JPEG, JPG, PNG, BMP, GIF)

        // 视频
        @JvmStatic
        fun ofVideo(): Set<MimeType> = EnumSet.of(MPEG, MP4, GPP, MKV, AVI)

        @JvmStatic
        fun isImage(mimeType: String?): Boolean = mimeType?.startsWith("image") == true

        @JvmStatic
        fun isVideo(mimeType: String?): Boolean = mimeType?.startsWith("video") == true

        private fun arraySetOf(vararg suffixes: String): Set<String> = ArraySet(Arrays.asList(*suffixes))
    }
}
