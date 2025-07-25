package com.cgfay.filter.glfilter.resource

import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.color.bean.DynamicColorBaseData
import com.cgfay.filter.glfilter.color.bean.DynamicColorData
import com.cgfay.filter.glfilter.effect.bean.DynamicEffect
import com.cgfay.filter.glfilter.effect.bean.DynamicEffectData
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData
import com.cgfay.filter.glfilter.makeup.bean.MakeupLipstickData
import com.cgfay.filter.glfilter.makeup.bean.MakeupMaterialData
import com.cgfay.filter.glfilter.makeup.bean.MakeupNormaData
import com.cgfay.filter.glfilter.makeup.bean.MakeupType
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerData
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerFrameData
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerNormalData
import com.cgfay.filter.glfilter.stickers.bean.StaticStickerNormalData
import com.cgfay.uitls.utils.FileUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList
import java.util.Iterator

/**
 * json解码器
 */
object ResourceJsonCodec {

    @Throws(IOException::class, JSONException::class)
    fun decodeStickerData(folderPath: String): DynamicSticker {
        val file = File(folderPath, "json")
        val stickerJson = FileUtils.convertToString(FileInputStream(file))
        val jsonObject = JSONObject(stickerJson)
        val dynamicSticker = DynamicSticker()
        dynamicSticker.unzipPath = folderPath
        if (dynamicSticker.dataList == null) {
            dynamicSticker.dataList = ArrayList()
        }
        val stickerList = jsonObject.getJSONArray("stickerList")
        for (i in 0 until stickerList.length()) {
            val jsonData = stickerList.getJSONObject(i)
            val type = jsonData.getString("type")
            val data: DynamicStickerData = when (type) {
                "sticker" -> {
                    val normalData = DynamicStickerNormalData()
                    val centerIndexList = jsonData.getJSONArray("centerIndexList")
                    normalData.centerIndexList = IntArray(centerIndexList.length())
                    for (j in 0 until centerIndexList.length()) {
                        normalData.centerIndexList[j] = centerIndexList.getInt(j)
                    }
                    normalData.offsetX = jsonData.getDouble("offsetX").toFloat()
                    normalData.offsetY = jsonData.getDouble("offsetY").toFloat()
                    normalData.baseScale = jsonData.getDouble("baseScale").toFloat()
                    normalData.startIndex = jsonData.getInt("startIndex")
                    normalData.endIndex = jsonData.getInt("endIndex")
                    normalData
                }
                "static" -> {
                    val normalData = StaticStickerNormalData()
                    normalData.alignMode = jsonData.getInt("alignMode")
                    normalData
                }
                else -> {
                    if ("frame" != type) continue
                    val frameData = DynamicStickerFrameData()
                    frameData.alignMode = jsonData.getInt("alignMode")
                    frameData
                }
            }
            data.width = jsonData.getInt("width")
            data.height = jsonData.getInt("height")
            data.frames = jsonData.getInt("frames")
            data.action = jsonData.getInt("action")
            data.stickerName = jsonData.getString("stickerName")
            data.duration = jsonData.getInt("duration")
            data.stickerLooping = jsonData.getInt("stickerLooping") == 1
            data.audioPath = jsonData.optString("audioPath")
            data.audioLooping = jsonData.optInt("audioLooping", 0) == 1
            data.maxCount = jsonData.optInt("maxCount", 5)
            dynamicSticker.dataList.add(data)
        }
        return dynamicSticker
    }

    @Throws(IOException::class, JSONException::class)
    fun decodeFilterData(folderPath: String): DynamicColor {
        val file = File(folderPath, "json")
        val filterJson = FileUtils.convertToString(FileInputStream(file))
        val jsonObject = JSONObject(filterJson)
        val dynamicColor = DynamicColor()
        dynamicColor.unzipPath = folderPath
        if (dynamicColor.filterList == null) {
            dynamicColor.filterList = ArrayList()
        }
        val filterList = jsonObject.getJSONArray("filterList")
        for (filterIndex in 0 until filterList.length()) {
            val filterData = DynamicColorData()
            val jsonData = filterList.getJSONObject(filterIndex)
            val type = jsonData.getString("type")
            if ("filter" == type) {
                filterData.name = jsonData.getString("name")
                filterData.vertexShader = jsonData.getString("vertexShader")
                filterData.fragmentShader = jsonData.getString("fragmentShader")
                val uniformList = jsonData.getJSONArray("uniformList")
                for (uniformIndex in 0 until uniformList.length()) {
                    val uniform = uniformList.getString(uniformIndex)
                    filterData.uniformList.add(uniform)
                }
                val uniformData = jsonData.getJSONObject("uniformData")
                if (uniformData != null) {
                    val dataIterator: Iterator<String> = uniformData.keys()
                    while (dataIterator.hasNext()) {
                        val key = dataIterator.next()
                        val value = uniformData.getString(key)
                        filterData.uniformDataList.add(DynamicColorBaseData.UniformData(key, value))
                    }
                }
                filterData.strength = jsonData.getDouble("strength").toFloat()
                filterData.texelOffset = jsonData.getInt("texelOffset") == 1
                filterData.audioPath = jsonData.getString("audioPath")
                filterData.audioLooping = jsonData.getInt("audioLooping") == 1
            }
            dynamicColor.filterList.add(filterData)
        }
        return dynamicColor
    }

    @Throws(IOException::class, JSONException::class)
    fun decodeMakeupData(folderPath: String): DynamicMakeup {
        val file = File(folderPath, "json")
        val makeupJson = FileUtils.convertToString(FileInputStream(file))
        val dynamicMakeup = DynamicMakeup()
        dynamicMakeup.unzipPath = folderPath
        if (dynamicMakeup.makeupList == null) {
            dynamicMakeup.makeupList = ArrayList()
        }
        val jsonObject = JSONObject(makeupJson)
        val makeupList = jsonObject.getJSONArray("makeupList")
        for (makeupIndex in 0 until makeupList.length()) {
            val jsonData = makeupList.getJSONObject(makeupIndex)
            val type = jsonData.getString("type")
            val makeupData: MakeupBaseData = if (type.equals("lipstick", ignoreCase = true)) {
                val lipstickData = MakeupLipstickData()
                lipstickData.lookupTable = jsonData.getString("lookupTable")
                lipstickData
            } else {
                val normaData = MakeupNormaData()
                val jsonMaterial = jsonData.getJSONObject("material")
                val materialData = MakeupMaterialData()
                materialData.name = jsonMaterial.getString("name")
                materialData.width = jsonMaterial.getInt("width")
                materialData.height = jsonMaterial.getInt("height")
                val textureVertices = jsonData.getJSONArray("textureVertices")
                materialData.textureVertices = FloatArray(textureVertices.length())
                for (i in 0 until textureVertices.length()) {
                    materialData.textureVertices[i] = textureVertices.getLong(i).toFloat()
                }
                val indices = jsonData.getJSONArray("indices")
                materialData.indices = ShortArray(indices.length())
                for (i in 0 until indices.length()) {
                    materialData.indices[i] = indices.getInt(i).toShort()
                }
                normaData.materialData = materialData
                normaData
            }
            makeupData.makeupType = MakeupType.getType(type)
            makeupData.name = jsonData.getString("name")
            makeupData.id = jsonData.getString("id")
            makeupData.strength = jsonData.getLong("strength")
            dynamicMakeup.makeupList.add(makeupData)
        }
        return dynamicMakeup
    }

    @Throws(IOException::class, JSONException::class)
    fun decodecEffectData(folderPath: String): DynamicEffect {
        val file = File(folderPath, "json")
        val effectJson = FileUtils.convertToString(FileInputStream(file))
        val dynamicEffect = DynamicEffect()
        dynamicEffect.unzipPath = folderPath
        if (dynamicEffect.effectList == null) {
            dynamicEffect.effectList = ArrayList()
        }
        val jsonObject = JSONObject(effectJson)
        val effectList = jsonObject.getJSONArray("effectList")
        for (effectIndex in 0 until effectList.length()) {
            val jsonData = effectList.getJSONObject(effectIndex)
            val type = jsonData.getString("type")
            if (type.equals("effect", ignoreCase = true)) {
                val effectData = DynamicEffectData()
                effectData.name = jsonData.getString("name")
                effectData.vertexShader = jsonData.getString("vertexShader")
                effectData.fragmentShader = jsonData.getString("fragmentShader")
                val uniformData = jsonData.getJSONObject("uniformData")
                if (uniformData != null) {
                    val dataIterator: Iterator<String> = uniformData.keys()
                    while (dataIterator.hasNext()) {
                        val key = dataIterator.next()
                        val effectValue = uniformData.getJSONArray(key)
                        val value = FloatArray(effectValue.length())
                        for (i in 0 until effectValue.length()) {
                            value[i] = effectValue.getDouble(i).toFloat()
                        }
                        effectData.uniformDataList.add(DynamicEffectData.UniformData(key, value))
                    }
                }
                val uniformSampler = jsonData.getJSONObject("uniformSampler")
                if (uniformSampler != null) {
                    val dataIterator: Iterator<String> = uniformSampler.keys()
                    while (dataIterator.hasNext()) {
                        val key = dataIterator.next()
                        val value = uniformSampler.getString(key)
                        effectData.uniformSamplerList.add(DynamicEffectData.UniformSampler(key, value))
                    }
                }
                effectData.texelSize = jsonData.getInt("texelSize") == 1
                effectData.duration = jsonData.getInt("duration")
                dynamicEffect.effectList.add(effectData)
            }
        }
        return dynamicEffect
    }
}
