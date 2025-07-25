package com.cgfay.filter.glfilter.resource

import android.text.TextUtils
import android.util.Log
import android.util.Pair

import com.cgfay.uitls.utils.FileUtils

import java.io.BufferedInputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 资源解码器
 */
open class ResourceCodec(protected var mIndexPath: String, protected var mDataPath: String) {

    protected var mIndexMap: Map<String, Pair<Int, Int>>? = null
    protected var mDataBuffer: ByteBuffer? = null

    companion object {
        protected const val TAG = "ResourceCodec"

        @JvmStatic
        fun getResourceFile(folder: String): Pair<String, String>? {
            var index: String? = null
            var data: String? = null
            val file = File(folder)
            val list = file.list() ?: return null
            for (name in list) {
                when (name) {
                    "index.idx" -> index = name
                    "resource.res" -> data = name
                }
            }
            return if (!TextUtils.isEmpty(index) && !TextUtils.isEmpty(data)) Pair(index!!, data!!) else null
        }

        @JvmStatic
        fun getFileFromZip(stream: InputStream): Map<String, ArrayList<FileDescription>>? {
            val map: HashMap<String, ArrayList<FileDescription>> = HashMap()
            val inputStream = ZipInputStream(BufferedInputStream(stream))
            try {
                val buffer = ByteArray(8192)
                var entry: ZipEntry?
                while (inputStream.nextEntry.also { entry = it } != null) {
                    if (!entry!!.isDirectory && !entry!!.name.endsWith(".DS_Store") && !entry!!.name.contains("__MACOSX") && !FileUtils.extractFileName(entry!!.name).startsWith(".") && entry!!.name.endsWith(".png")) {
                        val folder = FileUtils.extractFileFolder(entry!!.name)
                        var folderList = map[folder]
                        if (folderList == null) {
                            folderList = ArrayList()
                            map[folder] = folderList
                        }
                        val bufferOffset = ByteArray(8192)
                        var offset = 0
                        var length: Int
                        while (inputStream.read(bufferOffset).also { length = it } != -1) {
                            offset += length
                        }
                        folderList.add(FileDescription(entry!!.name, offset.toLong()))
                    }
                }
            } finally {
                inputStream.close()
            }
            return map
        }

        @JvmStatic
        @Throws(IOException::class)
        fun unzipToFolder(inputStream: InputStream, folder: File, dirList: Map<String, ArrayList<FileDescription>>) {
            val offsetMap = HashMap<String, Any>()
            val sizeMap = HashMap<String, Any>()
            val iterator: Iterator<*> = dirList.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next() as Map.Entry<*, *>
                Collections.sort(entry.value as List<FileDescription>) { item1, item2 -> item1.fileName.compareTo(item2.fileName) }
                FileUtils.makeDirectory(folder.toString() + "/" + entry.key as String)
                var offset = 16
                val offsetHashMap = HashMap<String, Int>()
                val sizeHashMap = HashMap<String, Int>()
                var fileDescription: FileDescription
                for (fileDesIterator in (entry.value as ArrayList<*>).iterator()) {
                    fileDescription = fileDesIterator as FileDescription
                    val fileName = FileUtils.extractFileName(fileDescription.fileName)
                    offsetHashMap[fileName] = offset
                    sizeHashMap[fileName] = fileDescription.size.toInt()
                    offset += fileDescription.size.toInt()
                }
                sizeMap[entry.key as String] = offsetHashMap
                val builder = StringBuilder()
                val indexIterator: Iterator<*> = offsetHashMap.entries.iterator()
                while (indexIterator.hasNext()) {
                    val indexEntry = indexIterator.next() as Map.Entry<*, *>
                    builder.append(indexEntry.key as String)
                        .append(':')
                        .append(indexEntry.value)
                        .append(':')
                        .append(sizeHashMap[indexEntry.key])
                        .append(';')
                }
                var success = false
                var outputStream: FileOutputStream? = null
                val file: File
                try {
                    file = File(folder.toString() + "/" + entry.key as String, "index.idx")
                    outputStream = FileOutputStream(file)
                    outputStream.write(builder.toString().toByteArray(charset("UTF-8")))
                    success = true
                } catch (e: Exception) {
                    Log.e(TAG, "writeLinesToFile failed!", e)
                } finally {
                    FileUtils.safetyClose(outputStream)
                }
                if (!success) {
                    throw IOException("write index file failed!")
                }
                val dataFile = File(folder.toString() + "/" + entry.key as String, "resource.res")
                val accessFile = RandomAccessFile(dataFile, "rw")
                for (i in 0 until 16) {
                    accessFile.write(0)
                }
                offsetMap[entry.key as String] = accessFile
            }
            val zipStream = ZipInputStream(BufferedInputStream(inputStream))
            try {
                val buffer = ByteArray(8192)
                while (true) {
                    val zipEntry = zipStream.nextEntry ?: break
                    if (zipEntry.isDirectory || zipEntry.name.endsWith(".DS_Store") || zipEntry.name.contains("__MACOSX") || FileUtils.extractFileName(zipEntry.name).startsWith(".")) {
                        continue
                    }
                    if (zipEntry.name.endsWith(".png")) {
                        val folderName = FileUtils.extractFileFolder(zipEntry.name)
                        val accessFile = offsetMap[folderName] as RandomAccessFile
                        val pos = (sizeMap[folderName] as Map<*, *>)[FileUtils.extractFileName(zipEntry.name)] as Int
                        accessFile.seek(pos.toLong())
                        var length: Int
                        while (zipStream.read(buffer).also { length = it } != -1) {
                            accessFile.write(buffer, 0, length)
                        }
                    } else {
                        val file = File(folder, zipEntry.name)
                        val folderFile = if (zipEntry.isDirectory) file else file.parentFile
                        if (!folderFile.isDirectory && !folderFile.mkdirs()) {
                            throw FileNotFoundException("Failed to find directory: " + folderFile.absolutePath)
                        }
                        val outputStream = FileOutputStream(file)
                        try {
                            var length: Int
                            while (zipStream.read(buffer).also { length = it } != -1) {
                                outputStream.write(buffer, 0, length)
                            }
                        } finally {
                            outputStream.close()
                        }
                    }
                }
            } finally {
                zipStream.close()
                val entryIterator: Iterator<*> = offsetMap.entries.iterator()
                while (entryIterator.hasNext()) {
                    val entry = entryIterator.next() as Map.Entry<*, *>
                    FileUtils.safetyClose(entry.value as Closeable)
                }
            }
        }
    }

    @Throws(IOException::class)
    open fun init() {
        mIndexMap = parseIndexFile(mIndexPath)
        val file = File(mDataPath)
        mDataBuffer = ByteBuffer.allocateDirect(file.length().toInt())
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(2048)
        var result = false
        try {
            var length: Int
            while (inputStream.read(buffer).also { length = it } != -1) {
                mDataBuffer!!.put(buffer, 0, length)
            }
            result = true
        } catch (e: IOException) {
            Log.e(TAG, "init: ", e)
        } finally {
            FileUtils.safetyClose(inputStream)
        }
        if (!result) {
            throw IOException("Failed to parse data file!")
        }
    }

    @Throws(IOException::class)
    private fun parseIndexFile(indexPath: String): Map<String, Pair<Int, Int>> {
        val indexString = FileUtils.convertToString(FileInputStream(File(indexPath)))
        val map: HashMap<String, Pair<Int, Int>> = HashMap()
        val indexArray = indexString.split(";")
        for (indexItem in indexArray) {
            if (!TextUtils.isEmpty(indexItem)) {
                val subIndexArray = indexItem.split(":")
                if (subIndexArray.size == 3) {
                    val offset = parseInt(subIndexArray[1], -1)
                    val length = parseInt(subIndexArray[2], -1)
                    if (offset == -1 || length == -1) {
                        throw IOException("Failed to parse offset or length for $indexItem")
                    }
                    map[subIndexArray[0]] = Pair(offset, length)
                }
            }
        }
        return map
    }

    private fun parseInt(str: String, defaultValue: Int): Int {
        var result = defaultValue
        try {
            result = str.toInt()
        } catch (e: NumberFormatException) {
            Log.e(TAG, "parseInt: ", e)
        }
        return result
    }
}

class FileDescription(var fileName: String, var size: Long)
