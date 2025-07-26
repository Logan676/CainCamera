package com.cgfay.picker.model

import androidx.collection.ArraySet
import androidx.compose.runtime.Stable
import java.util.Arrays
import java.util.EnumSet
import java.util.Set

/**
 * Supported media mime types.
 */
@Stable
enum class MimeType(val mimeType: String, val extensions: Set<String>) {
    // Images
    JPEG("image/jpeg", arraySetOf("jpeg")),
    JPG("image/jpg", arraySetOf("jpg")),
    BMP("image/bmp", arraySetOf("bmp")),
    PNG("image/png", arraySetOf("png")),
    GIF("image/gif", arraySetOf("gif")),

    // Videos
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

        @JvmStatic
        fun ofImage(): Set<MimeType> = EnumSet.of(JPEG, JPG, PNG, BMP, GIF)

        @JvmStatic
        fun ofVideo(): Set<MimeType> = EnumSet.of(MPEG, MP4, GPP, MKV, AVI)

        @JvmStatic
        fun isImage(mimeType: String?): Boolean = mimeType?.startsWith("image") == true

        @JvmStatic
        fun isVideo(mimeType: String?): Boolean = mimeType?.startsWith("video") == true

        private fun arraySetOf(vararg suffixes: String): Set<String> = ArraySet(Arrays.asList(*suffixes))
    }
}
