package com.cgfay.picker.widget.subsamplingview.decoder

interface DecoderFactory<T> {
    @Throws(IllegalAccessException::class, InstantiationException::class)
    fun make(): T
}
