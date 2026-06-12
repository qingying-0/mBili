package com.QYqx.mbili.customComponent.mDownLoader
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

/** 下载状态密封类 */
sealed class DownloadStatus {
    data class Started(val totalSize: Long) : DownloadStatus()
    // 对象：无参数的状态（单例）
    object Paused : DownloadStatus()
    data class Progress(val current: Long, val total: Long, val percent: Float) : DownloadStatus()
    data class Completed(val fileUri: Uri) : DownloadStatus()
    data class Failed(val exception: Exception) : DownloadStatus()
}