package com.QYqx.mbili.module.video.module.videoPlayer

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.customComponent.mDownLoader.DownloadStatus
import com.QYqx.mbili.customComponent.mDownLoader.MultiFileDownloadManager
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.module.video.module.videoPlayer.util.DanmuXmlParser
import com.QYqx.mbili.module.video.module.videoPlayer.util.DeflateInterceptor
import com.QYqx.mbili.network.NetworkApi
import com.QYqx.mbili.network.base.BaseResponse
import com.QYqx.mbili.network.bean.Danmu
import com.QYqx.mbili.network.bean.VideoDetailBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class VideoPlayerRepository {
    private val TAG = "VideoPlayerRepo"
    // OkHttp客户端（弹幕请求专用）
    private val danmuClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(DeflateInterceptor())
            .build()
    }

    /**
     * 获取视频播放地址
     */
    suspend fun getVideoStreamUrl(bvid: String, cid: Long): String? = withContext(Dispatchers.IO) {
        val videoStream = NetworkApi.getSignedVideoStream(
            bvid = bvid,
            cid = cid,
            qn = 80,
            fnval = 16
        )
        if (videoStream?.code == 0 && videoStream.data != null) {
            videoStream.data.durl?.get(0)?.url
        } else {
            null
        }
    }



    /**
     * 请求并解析B站弹幕数据
     */
    suspend fun fetchDanmuList(cid: Long): List<Danmu> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://comment.bilibili.com/$cid.xml")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Accept", "text/xml,application/xml,application/xhtml+xml")
                .build()

            val response = danmuClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val rawXml = response.body!!.string()
                DanmuXmlParser.parseDanmuByRegex(rawXml)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 视频下载（Flow形式返回下载状态）
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadVideo(context: Context, url: String, fileName: String): Flow<DownloadStatus> = flow {
        val downloadManager = MultiFileDownloadManager.INSTANCE
        val fileUri = downloadManager.createDownloadFileUri(
            context,
            fileName,
            "video/mp4"
        ) ?: return@flow

        downloadManager.downloadFile(url, fileUri).collect {
            emit(it)
        }
    }
    // 挂起函数：网络请求必须在协程中执行，此处不使用runBlocking（避免阻塞线程）
    suspend fun requestVideoDetail(bvid: String): BaseResponse<VideoDetailBean> {
        return NetworkApi.getVideoDetailByBvid(bvid)
    }
    // 挂起函数：网络请求必须在协程中执行，此处不使用runBlocking（避免阻塞线程）
    suspend fun requestVideoList(pageSize: Int = 14): List<VideoCard> {
        val result = NetworkApi.requestVideoList(pageSize)
        val videoCardList = mutableListOf<VideoCard>()
        // 安全判空，避免空指针异常
        result.data?.item?.forEach { item ->
            val videoCard = VideoCard(
                title = item.title,
                picUrl_4_3 = item.pic_4_3,
                picUrl = item.pic,
                playNum = item.stat.view,
                danmuNum = item.stat.danmaku,
                time = item.duration,
                upName = item.owner.name,
                bvid = item.bvid,
                cid = item.cid
            )
            videoCardList.add(videoCard)
        }
        return videoCardList
    }
}