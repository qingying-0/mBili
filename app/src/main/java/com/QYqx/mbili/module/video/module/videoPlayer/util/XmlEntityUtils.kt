package com.QYqx.mbili.module.video.module.videoPlayer.util

object XmlEntityUtils {
    /**
     * 清理XML中的非法实体字符，修复未转义的特殊字符
     * @param xmlContent 原始XML字符串
     * @return 清理后的合法XML字符串
     */
    fun cleanInvalidXmlEntities(xmlContent: String): String {
        // 步骤1：先替换独立的&（未转义的&，不是实体引用的一部分）
        val cleaned = xmlContent.replace(Regex("&(?!amp;|lt;|gt;|quot;|apos;)"), "&amp;")
        // 步骤2：修复其他可能的非法字符
        return cleaned
            .replace("<", "&lt;")    // 小于号
            .replace(">", "&gt;")    // 大于号
            .replace("\"", "&quot;") // 双引号
            .replace("'", "&apos;")  // 单引号
    }

    /**
     * 还原弹幕文本中的XML实体字符（解析后还原为原始字符）
     * @param text 解析后的弹幕文本
     * @return 还原后的原始文本
     */
    fun restoreXmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }
}