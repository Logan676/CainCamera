package com.cgfay.filter.glfilter.resource

import android.content.Context
import android.content.res.AssetManager
import android.util.Log

import com.cgfay.uitls.utils.FileUtils

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.Map

/**
 * 资源助手基类
 */
open class ResourceBaseHelper {

    companion object {
        private const val TAG = "ResourceBaseHelper"
    }

    /**
     * 解压Asset文件夹目录下的资源
     */
    protected fun decompressAsset(
        context: Context,
        assetName: String,
        unzipFolder: String,
        parentFolder: String
    ) {
        if (File("$parentFolder/$unzipFolder").exists()) {
            Log.d(TAG, "decompressAsset: directory $unzipFolder is existed!")
            return
        }
        val manager: AssetManager = context.assets
        var inputStream: InputStream? = null
        try {
            inputStream = manager.open(assetName)
            val dirList = ResourceCodec.getFileFromZip(inputStream)
            inputStream.close()
            if (dirList == null) return
            inputStream = manager.open(assetName)
            ResourceCodec.unzipToFolder(inputStream, File(parentFolder), dirList)
        } catch (e: IOException) {
            Log.e(TAG, "decompressAsset: ", e)
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 解压绝对路径目录下的资源
     */
    protected fun decompressFile(zipPath: String, unzipPath: String, parentFolder: String) {
        if (File("$parentFolder/$unzipPath").exists()) {
            Log.d(TAG, "decompressFile: directory ${unzipPath}is existed!")
            return
        }
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(zipPath)
            val dirList = ResourceCodec.getFileFromZip(inputStream)
            inputStream.close()
            if (dirList == null) return
            inputStream = FileInputStream(zipPath)
            ResourceCodec.unzipToFolder(inputStream, File(parentFolder), dirList)
        } catch (e: IOException) {
            Log.e(TAG, "decompressFile: ", e)
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
