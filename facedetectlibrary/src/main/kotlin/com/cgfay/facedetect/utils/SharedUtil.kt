package com.cgfay.facedetect.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Save Data To SharedPreferences
 */
class SharedUtil(private val ctx: Context) {
    private val fileName = "megvii"

    fun saveIntValue(key: String, value: Int) {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharePre.edit().putInt(key, value).apply()
    }

    fun saveLongValue(key: String, value: Long) {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharePre.edit().putLong(key, value).apply()
    }

    fun writeDownStartApplicationTime() {
        val sp = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        sp.edit().putLong("nowtimekey", now).apply()
    }

    fun saveBooleanValue(key: String, value: Boolean) {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharePre.edit().putBoolean(key, value).apply()
    }

    fun removeSharePreferences(key: String) {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharePre.edit().remove(key).apply()
    }

    operator fun contains(key: String): Boolean {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharePre.contains(key)
    }

    fun getAllMap(): Map<String, *> {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharePre.all
    }

    fun getIntValueByKey(key: String): Int {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharePre.getInt(key, -1)
    }

    fun getLongValueByKey(key: String): Long {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharePre.getLong(key, -1)
    }

    fun saveStringValue(key: String, value: String?) {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        sharePre.edit().putString(key, value).apply()
    }

    fun getStringValueByKey(key: String): String? {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharePre.getString(key, null)
    }

    fun getBooleanValueByKey(key: String): Boolean {
        val sharePre = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        return sharePre.getBoolean(key, false)
    }

    fun getIntValueAndRemoveByKey(key: String): Int {
        val value = getIntValueByKey(key)
        removeSharePreferences(key)
        return value
    }

    fun setUserkey(userkey: String) {
        this.saveStringValue("params_userkey", userkey)
    }

    val userkey: String?
        get() = this.getStringValueByKey("params_userkey")
}
