package com.cgfay.uitls.utils

import android.util.Log
import android.text.TextUtils
import java.io.*

/**
 * 文件操作
 * Created by cain on 2017/10/4.
 */
object FileUtils {
    private const val TAG = "FileUtils"
    private const val BUFFER_SIZE = 1024 * 8

    /** 检查文件是否存在 */
    @JvmStatic
    fun fileExists(path: String?): Boolean {
        return if (path == null) {
            false
        } else File(path).exists()
    }

    /** 检查文件列表是否存在 */
    @JvmStatic
    fun fileExists(paths: Array<String>): Boolean {
        for (path in paths) {
            if (!fileExists(path)) {
                return false
            }
        }
        return true
    }

    /** 解码得到文件名 */
    @JvmStatic
    fun extractFileName(path: String): String {
        val index = path.lastIndexOf('/')
        return if (index < 0) path else path.substring(index + 1)
    }

    /** 解码得到文件夹名 */
    @JvmStatic
    fun extractFileFolder(folderPath: String): String {
        val length = folderPath.length
        val index = folderPath.lastIndexOf('/')
        if (index == -1 || folderPath[length - 1] == '/') {
            return folderPath
        }
        if (folderPath.indexOf('/') == index && folderPath[0] == '/') {
            return folderPath.substring(0, index + 1)
        }
        return folderPath.substring(0, index)
    }

    /** 从Stream中获取String */
    @JvmStatic
    @Throws(IOException::class)
    fun convertToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val builder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            builder.append(line).append('\n')
        }
        return builder.toString()
    }

    /** 关闭Reader */
    @JvmStatic
    fun safetyClose(closeable: Closeable?): Boolean {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (var2: IOException) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun copyFile(oldPath: String, newPath: String) {
        var `is`: InputStream? = null
        var fs: FileOutputStream? = null
        try {
            val oldFile = File(oldPath)
            if (oldFile.exists()) {
                `is` = FileInputStream(oldPath)
                fs = FileOutputStream(newPath)
                val buffer = ByteArray(BUFFER_SIZE)
                var len: Int
                while (`is`.read(buffer).also { len = it } != -1) {
                    fs.write(buffer, 0, len)
                }
                fs.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (fs != null) {
                try {
                    fs.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /** 创建 .nomedia 文件 */
    @JvmStatic
    fun createNoMediaFile(path: String) {
        val file = createFile(path, ".nomedia")
        try {
            file?.createNewFile()
        } catch (e: IOException) {
            Log.e(TAG, "createNoMediaFile:  failed to create nomedia file")
        }
    }

    /** 创建文件 */
    @JvmStatic
    fun createFile(folderPath: String?, name: String?): File? {
        if (folderPath == null || name == null) {
            return null
        }
        if (!makeDirectory(folderPath)) {
            Log.e(TAG, "create parent directory failed, $folderPath")
            return null
        }
        val str = "$folderPath/$name"
        return File(str)
    }

    /** 创建文件名 */
    @JvmStatic
    fun createFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /** 创建目录/文件 */
    @JvmStatic
    fun makeDirectory(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) file.isDirectory else file.mkdirs()
    }

    /** 删除文件 */
    @JvmStatic
    fun deleteFile(fileName: String?): Boolean {
        return if (TextUtils.isEmpty(fileName)) {
            false
        } else deleteFile(File(fileName))
    }

    /** 删除文件 */
    @JvmStatic
    fun deleteFile(file: File?): Boolean {
        var result = true
        if (file != null) {
            result = file.delete()
        }
        return result
    }

    /** 删除目录 */
    @JvmStatic
    fun deleteDir(path: File?): Boolean {
        if (path != null && path.exists() && path.isDirectory) {
            for (file in path.listFiles()) {
                if (file.isDirectory) deleteDir(file)
                file.delete()
            }
            return path.delete()
        }
        return false
    }

    /** 删除目录 */
    @JvmStatic
    fun deleteDir(path: String?): Boolean {
        return if (path != null && path.isNotEmpty()) {
            deleteDir(File(path))
        } else false
    }

    /** 剪切文件 */
    @JvmStatic
    fun moveFile(oldPath: String?, newPath: String?): Boolean {
        if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(newPath)) {
            return false
        }
        val file = File(oldPath)
        return file.renameTo(File(newPath))
    }
}
