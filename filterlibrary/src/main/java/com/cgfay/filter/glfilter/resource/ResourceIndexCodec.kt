package com.cgfay.filter.glfilter.resource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Pair

import java.io.IOException
import java.util.Iterator
import java.util.Map

/**
 * 索引读取器
 */
class ResourceIndexCodec(indexPath: String, dataPath: String) : ResourceCodec(indexPath, dataPath) {

    private lateinit var mIndexArrays: IntArray
    private lateinit var mSizeArrays: IntArray

    @Throws(IOException::class)
    override fun init() {
        super.init()
        var length = 0
        for (iterator in mIndexMap!!.keys) {
            length = Math.max(getIndex(iterator), length)
        }
        mIndexArrays = IntArray(length + 1)
        mSizeArrays = IntArray(length + 1)
        for (i in mIndexArrays.indices) {
            mIndexArrays[i] = -1
            mSizeArrays[i] = -1
        }
        for (entry in mIndexMap!!.entries) {
            val index = getIndex(entry.key)
            if (index >= 0 && index < mIndexArrays.size) {
                mIndexArrays[index] = entry.value.first
                mSizeArrays[index] = entry.value.second
            }
        }
    }

    private fun getIndex(fileName: String): Int {
        val indexStr = fileName.substring(fileName.length - 7, fileName.length - 4)
        var index = 0
        try {
            index = indexStr.toInt()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "getIndex: ", e)
        }
        return index
    }

    fun loadResource(index: Int): Bitmap? {
        if (index < 0 || index >= mIndexArrays.size) {
            return null
        }
        val pos = mIndexArrays[index]
        val size = mSizeArrays[index]
        return if (pos == -1 || size == -1) {
            null
        } else BitmapFactory.decodeByteArray(mDataBuffer!!.array(), mDataBuffer!!.arrayOffset() + pos, size)
    }
}
