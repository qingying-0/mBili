package com.QYqx.mbili.network.bean

data class Danmu(
    val time: Float,       // 弹幕出现时间（秒）
    val type: Int,         // 弹幕类型（1-滚动，4-底部，5-顶部）
    val fontSize: Int,     // 字体大小
    val color: Int,        // 字体颜色（十进制RGB）
    val sendTime: Long,    // 发送时间戳
    val poolType: Int,     // 弹幕池类型
    val userIdHash: String,// 用户ID哈希
    val dmid: Long,        // 弹幕唯一ID
    val text: String,       // 弹幕文本内容
    var isShown: Boolean = false, // 新增：标记是否已显示
    val wasHandled: Boolean = false // 标记是否已处理显示，避免重复
)