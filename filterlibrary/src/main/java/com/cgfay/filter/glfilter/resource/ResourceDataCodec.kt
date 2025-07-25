package com.cgfay.filter.glfilter.resource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair

/**
 * 数据解码器
 */
class ResourceDataCodec(indexPath: String, dataPath: String) : ResourceCodec(indexPath, dataPath) {

    fun loadBitmap(name: String): Bitmap? {
        val pair = mIndexMap?.get(name) ?: return null
        return BitmapFactory.decodeByteArray(mDataBuffer!!.array(), mDataBuffer!!.arrayOffset() + pair.first, pair.second)
    }

    fun getBufferArray(): ByteArray? = mDataBuffer?.array()

    fun getResourcePair(path: String): Pair<Int, Int>? {
        val pair = mIndexMap?.get(path) ?: return null
        return Pair(pair.first + mDataBuffer!!.arrayOffset(), pair.second)
    }
}
