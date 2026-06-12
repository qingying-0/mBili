package com.QYqx.mbili.module.video.module.videoPlayer.util
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import android.util.Log
import com.QYqx.mbili.network.bean.Danmu
object DanmuXmlParser {
    private val TAG = "DanmuXmlParser"
    fun parseDanmuByRegex(xmlString: String): List<Danmu> {
        val danmuList = mutableListOf<Danmu>()
        val pattern = Regex("<d p=\"([^\"]+)\">([^<]+)</d>")
        val matches = pattern.findAll(xmlString)

        for (match in matches) {
            val pAttr = match.groupValues[1]
            val text = match.groupValues[2].trim()
            val pValues = pAttr.split(",")

            if (pValues.size >= 8 && text.isNotBlank()) {
                val time = pValues[0].toFloatOrNull() ?: 0f
                val type = pValues[1].toIntOrNull() ?: 1
                val fontSize = pValues[2].toIntOrNull() ?: 25
                val color = pValues[3].toIntOrNull() ?: 0xFFFFFFFF.toInt()
                val sendTime = pValues[4].toLongOrNull() ?: 0L
                val poolType = pValues[5].toIntOrNull() ?: 0
                val userIdHash = pValues[6] ?: ""
                val dmid = pValues[7].toLongOrNull() ?: 0L

                danmuList.add(
                    Danmu(time, type, fontSize, color, sendTime, poolType, userIdHash, dmid, text)
                )
            }
        }
        Log.d(TAG, "正则解析出弹幕数: ${danmuList.size}")
        return danmuList.sortedBy { it.time }
    }

    fun parseDanmuXmlFromString(xmlString: String): List<Danmu> {
        val danmuList = mutableListOf<Danmu>()
        try {
            val cleanedXml = XmlEntityUtils.cleanInvalidXmlEntities(xmlString)
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(cleanedXml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                try {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if ("d" == parser.name) {
                                val pAttr = parser.getAttributeValue(null, "p")
                                if (pAttr.isNullOrEmpty()) {
                                    eventType = parser.next()
                                    continue
                                }

                                val pValues = pAttr.split(",")
                                // 兼容8/9/10个字段的情况（B站弹幕p属性字段数可能扩展）
                                if (pValues.size >= 8) {
                                    try {
                                        // 核心字段解析（忽略扩展字段）
                                        val time = pValues[0].toFloat()
                                        val type = pValues[1].toInt()
                                        val fontSize = pValues[2].toInt()
                                        val color = pValues[3].toInt()
                                        val sendTime = pValues[4].toLong()
                                        val poolType = pValues[5].toInt()
                                        val userIdHash = pValues[6]
                                        val dmid = pValues[7].toLong()
                                        var text = parser.nextText().trim()
                                        text = XmlEntityUtils.restoreXmlEntities(text)

                                        if (text.isNotBlank()) {
                                            danmuList.add(
                                                Danmu(
                                                    time = time,
                                                    type = type,
                                                    fontSize = fontSize,
                                                    color = color,
                                                    sendTime = sendTime,
                                                    poolType = poolType,
                                                    userIdHash = userIdHash,
                                                    dmid = dmid,
                                                    text = text
                                                )
                                            )
                                        }
                                    } catch (e: NumberFormatException) {
                                        // 忽略数值转换错误（字段格式异常）
                                        Log.d(TAG, "单条弹幕数值解析失败: ${e.message}")
                                    } catch (e: Exception) {
                                        Log.d(TAG, "单条弹幕解析失败: ${e.message}")
                                    }
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                } catch (e: Exception) {
                    // 只记录严重错误，忽略轻微的解析警告
                    if (e.message?.contains("Unexpected token") == true) {
                        Log.d(TAG, "XML解析轻微警告（不影响使用）: ${e.message?.substring(0, 50)}...")
                    } else {
                        Log.w(TAG, "XML解析节点异常", e)
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "XML解析严重异常", e)
        }

        Log.d(TAG, "最终解析出${danmuList.size}条有效弹幕")
        return danmuList.sortedBy { it.time }
    }
}