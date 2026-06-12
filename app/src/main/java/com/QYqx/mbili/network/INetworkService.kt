package com.QYqx.mbili.network


import com.QYqx.mbili.network.base.BaseResponse
import com.QYqx.mbili.network.bean.Player
import com.QYqx.mbili.network.bean.RecommendBean
import com.QYqx.mbili.network.bean.UserInfo
import com.QYqx.mbili.network.bean.VideoDetailBean
import com.QYqx.mbili.network.bean.VideoStreamResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface INetworkService {
//    @GET("videodetail")
//    suspend fun requestVideoDetail(@Query("id") id: String): BaseResponse<VideoBean>
    //https://api.bilibili.com/x/web-interface/index/top/rcmd?version=1&ps=5
    @GET("web-interface/index/top/rcmd?version=1")
    suspend fun requestVideoList(@Query("ps") num: Int): BaseResponse<RecommendBean>
    @GET("web-interface/nav")
    suspend fun requestUserInfo(): BaseResponse<UserInfo>
    @GET("player/playurl")
    suspend fun  requestVideoPlayerUrl(
        @Query("cid") cid: String,
        @Query("bvid") bvid: String,
        @Query("fnval") fnval: Int =0,
        @Query("fourk") fourk: Int =1
    ): BaseResponse<Player>
    /**
     * 获取视频流地址
     * @param bvid 视频BV号（与avid二选一）
     * @param cid 分P视频ID（必填）
     * @param qn 清晰度标识（文档中qn表，DASH格式无效）
     * @param fnval 格式标识（二进制OR运算，文档中fnval表）
     * @param fnver 版本标识（恒为0）
     * @param fourk 是否支持4K（1=支持，需会员）
     * @param platform 播放平台（html5=无防盗链验证）
     */
    @GET("player/wbi/playurl")
    suspend fun getSignedVideoStream(@QueryMap params: Map<String, String>): VideoStreamResponse
    /**
     * 根据BV号获取视频基础信息（非超详细，官方标准接口）
     * @param bvid 视频BV号（与aid二选一，推荐使用bvid）
     * @param aid 视频AV号（与bvid二选一，可不传）
     * @return 返回视频基础信息包装体
     */
    @GET("web-interface/view")
    suspend fun getVideoDetailByBvid(
        @Query("bvid") bvid: String,
        @Query("aid") aid: Long? = null
    ): BaseResponse<VideoDetailBean>
}