package com.QYqx.mbili.network.util.Wbi

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private val hexDigits = "0123456789abcdef".toCharArray()

fun ByteArray.toHexString() = buildString(this.size shl 1) {
    this@toHexString.forEach { byte ->
        append(hexDigits[byte.toInt() ushr 4 and 15])
        append(hexDigits[byte.toInt() and 15])
    }
}

fun String.toMD5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}

fun encodeURIComponent(value: String): String {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}

fun Map<String, Any?>.toQueryString() = this.filterValues { it != null }.entries.joinToString("&") { (k, v) ->
    "${encodeURIComponent(k)}=${if(v!=null)encodeURIComponent(v.toString())else ""}"
}