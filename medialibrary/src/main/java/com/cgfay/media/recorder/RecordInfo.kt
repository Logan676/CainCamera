package com.cgfay.media.recorder


/**
 * Information describing a recorded media segment.
 */
data class RecordInfo @JvmOverloads constructor(
data class RecordInfo @JvmOverloads constructor(
    val fileName: String,
    val duration: Long = -1L,
    val type: MediaType
)
