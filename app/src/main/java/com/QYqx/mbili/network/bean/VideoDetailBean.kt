package com.QYqx.mbili.network.bean


/**
 * 视频基础详情实体（对应x/web-interface/view接口data字段）
 */

data class VideoDetailBean(
    // 视频标识信息
    val bvid: String,                // BV号
    val aid: Long,                  // AV号
    val cid: Long,                  // 分PID（播放视频必填参数）
    // 视频基础信息
    val title: String,              // 视频标题
    val desc: String,               // 视频简介
    val pic: String,                // 视频封面图URL
    val pubdate: Long,              // 发布时间戳
    val duration: Int,              // 视频时长（秒）
    // 播放/互动数据
    val stat: VideoStatBean,      // UP主信息
    // UP主信息
    val owner: VideoOwnerBean,      // UP主信息
    // 分P信息
    val pages: List<VideoPageBean>  // 视频分P列表
)
data class VideoStatBean(
    val view: Long,                 // 播放量
    val danmaku: Long,              // 弹幕数
    val reply: Long,                // 评论数
    val favorite: Long,             // 收藏数
    val coin: Long,                 // 投币数
    val like: Long,                 // 点赞数
    val share: Long,                // 分享数
)
/**
 * UP主信息实体
 */

data class VideoOwnerBean(
    val mid: Long,        // UP主ID
    val name: String,     // UP主昵称
    val face: String      // UP主头像URL
)

/**
 * 视频分P实体
 */

data class VideoPageBean(
    val cid: Long,        // 分P的cid
    val page: Int,        // 分P序号
    val part: String,     // 分P标题
    val duration: Int     // 分P时长（秒）
)