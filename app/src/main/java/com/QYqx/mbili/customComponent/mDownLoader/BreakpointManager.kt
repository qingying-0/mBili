package com.QYqx.mbili.customComponent.mDownLoader

import com.QYqx.mbili.customComponent.mDownLoader.BreakpointManager.md5
import com.google.gson.Gson
import com.tencent.mmkv.MMKV

import java.security.MessageDigest

object BreakpointManager  {
    private val mmkv by lazy { MMKV.defaultMMKV() }
    private val gson = Gson()

    private fun generateKey(url: String, uriKey: String): String {
        val input = "$url|$uriKey"
        return input.md5()
    }

    private fun String.md5(): String {
        return MessageDigest.getInstance("MD5")
            .digest(toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }


    fun saveBreakpoint(record: DownloadBreakpointRecord) {
        val key = generateKey(record.url, record.fileUriKey)
        mmkv.encode(key, gson.toJson(record))
    }

    fun getBreakpoint(url: String, uriKey: String): DownloadBreakpointRecord? {
        val key = generateKey(url, uriKey)
        val json = mmkv.decodeString(key) ?: return null
        return try {
            gson.fromJson(json, DownloadBreakpointRecord::class.java)
        } catch (e: Exception) {
            // 可选：清理损坏数据
            mmkv.removeValueForKey(key)
            null
        }
    }

    fun deleteBreakpoint(url: String, uriKey: String) {
        val key = generateKey(url, uriKey)
        mmkv.removeValueForKey(key)
    }
}