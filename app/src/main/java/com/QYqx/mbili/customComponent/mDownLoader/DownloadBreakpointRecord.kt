package com.QYqx.mbili.customComponent.mDownLoader

// 断点记录（持久化到本地，用于下次继续下载）
/** 断点记录实体类（适配Uri） */
data class DownloadBreakpointRecord(
    val url: String,
    val fileUriKey: String,   // 使用Uri字符串替代文件路径
    val totalFileSize: Long,
    val chunkSize: Long,
    val completedChunks: List<Int>,
    val currentTotalDownloaded: Long
)