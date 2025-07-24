package com.cgfay.picker.widget.subsamplingview.decoder

class CompatDecoderFactory<T>(private val clazz: Class<out T>) : DecoderFactory<T> {
    @Throws(IllegalAccessException::class, InstantiationException::class)
    override fun make(): T {
        return clazz.newInstance()
    }
}
