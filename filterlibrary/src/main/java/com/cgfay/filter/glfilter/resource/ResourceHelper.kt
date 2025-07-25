package com.cgfay.filter.glfilter.resource

import android.content.Context
import android.os.Environment
import android.text.TextUtils

import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.glfilter.resource.bean.ResourceType
import com.cgfay.uitls.utils.FileUtils

import java.io.File

/**
 * 资源数据助手
 */
object ResourceHelper : ResourceBaseHelper() {

    private const val ResourceDirectory = "Resource"
    private val mResourceList = mutableListOf<ResourceData>()

    @JvmStatic
    fun getResourceList(): List<ResourceData> = mResourceList

    @JvmStatic
    fun initAssetsResource(context: Context) {
        FileUtils.createNoMediaFile(getResourceDirectory(context))
        mResourceList.clear()
        mResourceList.add(ResourceData("cat", "assets://resource/cat.zip", ResourceType.STICKER, "cat", "assets://thumbs/resource/cat.png"))
        mResourceList.add(ResourceData("test_sticker1", "assets://resource/test_sticker1.zip", ResourceType.STICKER, "test_sticker1", "assets://thumbs/resource/sticker_temp.png"))
        mResourceList.add(ResourceData("triple_frame", "assets://resource/triple_frame.zip", ResourceType.FILTER, "triple_frame", "assets://thumbs/resource/triple_frame.png"))
        mResourceList.add(ResourceData("horizontal_mirror", "assets://resource/horizontal_mirror.zip", ResourceType.FILTER, "horizontal_mirror", "assets://thumbs/resource/horizontal_mirror.png"))
        mResourceList.add(ResourceData("vertical_mirror", "assets://resource/vertical_mirror.zip", ResourceType.FILTER, "vertical_mirror", "assets://thumbs/resource/vertical_mirror.png"))
        decompressResource(context, mResourceList)
    }

    @JvmStatic
    fun decompressResource(context: Context, resourceList: List<ResourceData>) {
        if (!checkResourceDirectory(context)) return
        val resourcePath = getResourceDirectory(context)
        for (item in resourceList) {
            if (item.type.index >= 0) {
                when {
                    item.zipPath.startsWith("assets://") -> decompressAsset(context, item.zipPath.substring("assets://".length), item.unzipFolder, resourcePath)
                    item.zipPath.startsWith("file://") -> decompressFile(item.zipPath.substring("file://".length), item.unzipFolder, resourcePath)
                }
            }
        }
    }

    private fun checkResourceDirectory(context: Context): Boolean {
        val resourcePath = getResourceDirectory(context)
        val file = File(resourcePath)
        return if (file.exists()) {
            file.isDirectory
        } else {
            file.mkdirs()
        }
    }

    @JvmStatic
    fun getResourceDirectory(context: Context): String {
        val resourcePath: String
        resourcePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(ResourceDirectory)?.absolutePath ?: (context.filesDir.path + File.separator + ResourceDirectory)
        } else {
            context.filesDir.toString() + File.separator + ResourceDirectory
        }
        return resourcePath
    }

    @JvmStatic
    fun deleteResource(context: Context, resource: ResourceData?): Boolean {
        if (resource == null || TextUtils.isEmpty(resource.unzipFolder)) {
            return false
        }
        if (!checkResourceDirectory(context)) {
            return false
        }
        val resourceFolder = getResourceDirectory(context) + File.separator + resource.unzipFolder
        val file = File(resourceFolder)
        return if (!file.exists() || !file.isDirectory) {
            false
        } else FileUtils.deleteDir(file)
    }
}
