package com.cgfay.media.recorder

import androidx.compose.runtime.Immutable

/**
 * Information describing a recorded media segment.
 */
@Immutable
data class RecordInfo @JvmOverloads constructor(
    val fileName: String,
    val duration: Long = -1L,
    val type: MediaType
)
