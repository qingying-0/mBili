package com.QYqx.mbili.module.video.module.videoPlayer

class VideoPlayerBean {
}
// 视频播放状态
enum class PlaybackStatus {
    IDLE,        // 空闲
    BUFFERING,   // 缓冲中
    READY,       // 准备完成
    PLAYING,     // 播放中
    PAUSED,      // 已暂停
    ENDED,       // 播放结束
    ERROR        // 播放异常
}
// 屏幕方向状态
enum class ScreenOrientation {
    PORTRAIT,    // 竖屏
    LANDSCAPE    // 横屏
}

data class MessageEvent_View(
    val bvid: String,
    val view: String
) {

}