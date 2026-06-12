package com.QYqx.mbili.customComponent.mDownLoader

// 分片任务信息
/** 分片任务实体类 */
data class ChunkTask(
    val chunkIndex: Int,      // 分片索引
    val start: Long,          // 起始偏移量
    val end: Long,            // 结束偏移量
    val chunkSize: Long,      // 分片大小
    val isCompleted: Boolean  // 是否完成
)