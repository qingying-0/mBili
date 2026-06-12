package com.QYqx.mbili.module.otherActivity.downloadList.entry

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DownloadItemEntry")
data class DownloadItemEntry(
    val imgUrl: String,
    val upName: String,//s
    val videoTitle: String,
    val videoTime: Int,
    @PrimaryKey
    val bvid: String,
    val url: String,
    val cid: Long,
    var videoLength: Long, //视频大小
    var downLoadProgress:Int, //0-100
    var running: Boolean = false
)