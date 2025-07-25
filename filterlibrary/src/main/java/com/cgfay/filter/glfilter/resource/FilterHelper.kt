package com.cgfay.filter.glfilter.resource

import android.content.Context
import android.os.Environment
import android.text.TextUtils

import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.glfilter.resource.bean.ResourceType
import com.cgfay.uitls.utils.FileUtils

import java.io.File

/**
 * 滤镜数据助手
 */
object FilterHelper : ResourceBaseHelper() {

    private const val FilterDirectory = "Filter"
    private val mFilterList = mutableListOf<ResourceData>()

    @JvmStatic
    fun getFilterList(): List<ResourceData> = mFilterList

    @JvmStatic
    fun initAssetsFilter(context: Context) {
        FileUtils.createNoMediaFile(getFilterDirectory(context))
        mFilterList.clear()
        mFilterList.add(ResourceData("none", "assets://filter/none.zip", ResourceType.NONE, "none", "assets://thumbs/filter/source.png"))
        mFilterList.add(ResourceData("amaro", "assets://filter/amaro.zip", ResourceType.FILTER, "amaro", "assets://thumbs/filter/amaro.png"))
        mFilterList.add(ResourceData("anitque", "assets://filter/anitque.zip", ResourceType.FILTER, "anitque", "assets://thumbs/filter/anitque.png"))
        mFilterList.add(ResourceData("blackcat", "assets://filter/blackcat.zip", ResourceType.FILTER, "blackcat", "assets://thumbs/filter/blackcat.png"))
        mFilterList.add(ResourceData("blackwhite", "assets://filter/blackwhite.zip", ResourceType.FILTER, "blackwhite", "assets://thumbs/filter/blackwhite.png"))
        mFilterList.add(ResourceData("brooklyn", "assets://filter/brooklyn.zip", ResourceType.FILTER, "brooklyn", "assets://thumbs/filter/brooklyn.png"))
        mFilterList.add(ResourceData("calm", "assets://filter/calm.zip", ResourceType.FILTER, "calm", "assets://thumbs/filter/calm.png"))
        mFilterList.add(ResourceData("cool", "assets://filter/cool.zip", ResourceType.FILTER, "cool", "assets://thumbs/filter/cool.png"))
        mFilterList.add(ResourceData("earlybird", "assets://filter/earlybird.zip", ResourceType.FILTER, "earlybird", "assets://thumbs/filter/earlybird.png"))
        mFilterList.add(ResourceData("emerald", "assets://filter/emerald.zip", ResourceType.FILTER, "emerald", "assets://thumbs/filter/emerald.png"))
        mFilterList.add(ResourceData("fairytale", "assets://filter/fairytale.zip", ResourceType.FILTER, "fairytale", "assets://thumbs/filter/fairytale.png"))
        mFilterList.add(ResourceData("freud", "assets://filter/freud.zip", ResourceType.FILTER, "freud", "assets://thumbs/filter/freud.png"))
        mFilterList.add(ResourceData("healthy", "assets://filter/healthy.zip", ResourceType.FILTER, "healthy", "assets://thumbs/filter/healthy.png"))
        mFilterList.add(ResourceData("hefe", "assets://filter/hefe.zip", ResourceType.FILTER, "hefe", "assets://thumbs/filter/hefe.png"))
        mFilterList.add(ResourceData("hudson", "assets://filter/hudson.zip", ResourceType.FILTER, "hudson", "assets://thumbs/filter/hudson.png"))
        mFilterList.add(ResourceData("kevin", "assets://filter/kevin.zip", ResourceType.FILTER, "kevin", "assets://thumbs/filter/kevin.png"))
        mFilterList.add(ResourceData("latte", "assets://filter/latte.zip", ResourceType.FILTER, "latte", "assets://thumbs/filter/latte.png"))
        mFilterList.add(ResourceData("lomo", "assets://filter/lomo.zip", ResourceType.FILTER, "lomo", "assets://thumbs/filter/lomo.png"))
        mFilterList.add(ResourceData("romance", "assets://filter/romance.zip", ResourceType.FILTER, "romance", "assets://thumbs/filter/romance.png"))
        mFilterList.add(ResourceData("sakura", "assets://filter/sakura.zip", ResourceType.FILTER, "sakura", "assets://thumbs/filter/sakura.png"))
        mFilterList.add(ResourceData("sketch", "assets://filter/sketch.zip", ResourceType.FILTER, "sketch", "assets://thumbs/filter/sketch.png"))
        mFilterList.add(ResourceData("sunset", "assets://filter/sunset.zip", ResourceType.FILTER, "sunset", "assets://thumbs/filter/sunset.png"))
        mFilterList.add(ResourceData("whitecat", "assets://filter/whitecat.zip", ResourceType.FILTER, "whitecat", "assets://thumbs/filter/whitecat.png"))
        decompressResource(context, mFilterList)
    }

    @JvmStatic
    fun decompressResource(context: Context, resourceList: List<ResourceData>) {
        if (!checkFilterDirectory(context)) return
        val filterPath = getFilterDirectory(context)
        for (item in resourceList) {
            if (item.type.index >= 0) {
                when {
                    item.zipPath.startsWith("assets://") -> decompressAsset(context, item.zipPath.substring("assets://".length), item.unzipFolder, filterPath)
                    item.zipPath.startsWith("file://") -> decompressFile(item.zipPath.substring("file://".length), item.unzipFolder, filterPath)
                }
            }
        }
    }

    private fun checkFilterDirectory(context: Context): Boolean {
        val resourcePath = getFilterDirectory(context)
        val file = File(resourcePath)
        return if (file.exists()) {
            file.isDirectory
        } else {
            file.mkdirs()
        }
    }

    @JvmStatic
    fun getFilterDirectory(context: Context): String {
        val resourcePath: String
        resourcePath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.getExternalFilesDir(FilterDirectory)?.absolutePath ?: (context.filesDir.path + File.separator + FilterDirectory)
        } else {
            context.filesDir.toString() + File.separator + FilterDirectory
        }
        return resourcePath
    }

    @JvmStatic
    fun deleteFilter(context: Context, resource: ResourceData?): Boolean {
        if (resource == null || TextUtils.isEmpty(resource.unzipFolder)) {
            return false
        }
        if (!checkFilterDirectory(context)) {
            return false
        }
        val resourceFolder = getFilterDirectory(context) + File.separator + resource.unzipFolder
        val file = File(resourceFolder)
        return if (!file.exists() || !file.isDirectory) {
            false
        } else FileUtils.deleteDir(file)
    }
}
