package com.QYqx.mbili.network.bean


data class VideoStreamResponse(
    val code: Int,
    val message: String,
    val data: VideoStreamData?
)

data class VideoStreamData(
    val format: String,
    val durl: List<DurlBean>?,
    val dash: DashBean?,
    val accept_quality: List<Int>?,
    val accept_description: List<String>?
)

data class DurlBean(val url: String, val backup_url: List<String>)
data class DashBean(val video: List<DashStreamBean>, val audio: List<DashStreamBean>?)
data class DashStreamBean(
    val baseUrl: String,
    val backupUrl: List<String>?,
    val mimeType: String,
    val codecs: String
)