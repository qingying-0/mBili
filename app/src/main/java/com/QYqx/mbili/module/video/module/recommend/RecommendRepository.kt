package com.QYqx.mbili.module.video.module.recommend

import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.network.NetworkApi

// 数据仓库：仅负责数据获取（网络/本地），不处理业务逻辑
class RecommendRepository {
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