package com.cgfay.filter.glfilter.resource

import android.content.Context
import android.os.Environment

import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.glfilter.resource.bean.ResourceType
import com.cgfay.uitls.utils.FileUtils

import java.io.File

/**
 * 彩妆数据助手
 */
object MakeupHelper : ResourceBaseHelper() {

    private const val MakeupDirectory = "Makeup"
    private val mMakeupList = mutableListOf<ResourceData>()

    @JvmStatic
    fun getMakeupList(): List<ResourceData> = mMakeupList

    @JvmStatic
    fun initAssetsMakeup(context: Context) {
        FileUtils.createNoMediaFile(getMakeupDirectory(context))
        mMakeupList.clear()
        mMakeupList.add(ResourceData("none", "assets://makeup/none.zip", ResourceType.NONE, "none", "assets://thumbs/makeup/none.png"))
        mMakeupList.add(ResourceData("ls01", "assets://makeup/ls01.zip", ResourceType.MAKEUP, "ls01", "assets://thumbs/makeup/ls01.png"))
        decompressResource(context, mMakeupList)
    }

    private fun decompressResource(context: Context, resourceList: List<ResourceData>) {
        if (!checkMakeupDirectory(context)) return
        val filterPath = getMakeupDirectory(context)
        for (item in resourceList) {
            if (item.type.index >= 0) {
                when {
                    item.zipPath.startsWith("assets://") -> decompressAsset(context, item.zipPath.substring("assets://".length), item.unzipFolder, filterPath)
                    item.zipPath.startsWith("file://") -> decompressFile(item.zipPath.substring("file://".length), item.unzipFolder, filterPath)
                }
            }
        }
    }

    private fun checkMakeupDirectory(context: Context): Boolean {
        val resourcePath = getMakeupDirectory(context)
        val file = File(resourcePath)
        return if (file.exists()) {
            file.isDirectory
        } else {
            file.mkdirs()
        }
    }

    @JvmStatic
    fun getMakeupDirectory(context: Context): String {
        val resourcePath: String
        resourcePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(MakeupDirectory)?.absolutePath ?: (context.filesDir.path + File.separator + MakeupDirectory)
        } else {
            context.filesDir.toString() + File.separator + MakeupDirectory
        }
        return resourcePath
    }
}
