package com.cgfay.media.recorder

data class RecordInfo(
    val fileName: String,
    val duration: Long = -1,
    val type: MediaType
)
