package com.QYqx.mbili.network.util.Wbi

import android.util.Base64
import java.security.MessageDigest
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.http.GET
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.network.INetworkService
import com.QYqx.mbili.network.NetworkApi
import java.util.*

/**
 * WBI 签名工具类（适配现有网络层）
 * 复用全局 Retrofit 和 CookieJar，实现密钥获取与参数签名
 */
object WbiSignUtil {
    // B站固定重排映射表
    private val MIXIN_KEY_ENC_TAB = intArrayOf(
        46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
        27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
        37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
        22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52
    )

    // 缓存 WBI 密钥（每日更替，缓存24小时）
    private var cachedWbiParams: WbiParams? = null
    private var cacheTimestamp: Long = 0

    /**
     * 获取 WBI 密钥（imgKey + subKey），复用现有 NetworkApi 的 Retrofit 实例
     */
    suspend fun getWbiParams(): WbiParams {
        // 缓存未过期直接返回
        if (cachedWbiParams != null && System.currentTimeMillis() - cacheTimestamp < 24 * 60 * 60 * 1000) {
            return cachedWbiParams!!
        }

        // 调用现有 nav 接口（requestUserInfo）获取 wbi_img 字段
        val userInfoResponse = NetworkApi.requestUserInfo()
        val wbiImg = userInfoResponse.data?.wbiImg ?: throw IllegalStateException("获取 WBI 密钥失败，请检查登录状态")

        // 从 URL 中截取 imgKey 和 subKey（去掉文件名后缀）
        val imgKey = wbiImg.imgUrl.substringAfterLast("/").substringBefore(".png")
        val subKey = wbiImg.subUrl.substringAfterLast("/").substringBefore(".png")

        // 缓存密钥
        val wbiParams = WbiParams(imgKey, subKey)
        cachedWbiParams = wbiParams
        cacheTimestamp = System.currentTimeMillis()
        return wbiParams
    }

    /**
     * 对请求参数进行 WBI 签名，生成 wts + w_rid
     * @param originParams 原始请求参数
     * @return 签名后的参数Map
     */
    fun signParams(originParams: MutableMap<String, Any>, wbiParams: WbiParams): Map<String, Any> {
        // 1. 生成秒级时间戳 wts
        val wts = (System.currentTimeMillis() / 1000).toString()
        originParams["wts"] = wts

        // 2. 构建混合密钥 mixinKey
        val rawKey = wbiParams.imgKey + wbiParams.subKey
        val mixinKey = buildString {
            repeat(32) { append(rawKey[MIXIN_KEY_ENC_TAB[it]]) }
        }

        // 3. 参数按key升序排序
        val sortedParams = originParams.toSortedMap()

        // 4. 拼接参数字符串（过滤特殊字符 + URL编码）
        val queryStr = sortedParams.entries.joinToString("&") { (key, value) ->
            val filteredValue = value.toString().replace(Regex("[!'()*]"), "")
            "${encodeURIComponent(key)}=${encodeURIComponent(filteredValue)}"
        }

        // 5. 生成 MD5 签名 w_rid
        val wRid = md5(queryStr + mixinKey)
        sortedParams["w_rid"] = wRid

        return sortedParams
    }

    /**
     * URL 编码（符合B站要求，字母大写）
     */
    private fun encodeURIComponent(str: String): String {
        return try {
            java.net.URLEncoder.encode(str, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~")
        } catch (e: Exception) {
            str
        }
    }

    /**
     * MD5 加密（小写格式）
     */
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * WBI 密钥数据类
     */
    data class WbiParams(val imgKey: String, val subKey: String)
}